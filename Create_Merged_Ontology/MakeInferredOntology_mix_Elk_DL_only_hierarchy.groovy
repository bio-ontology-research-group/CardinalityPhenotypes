@Grapes([
          @Grab(group='org.slf4j', module='slf4j-simple', version='1.6.1'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='4.2.5'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='4.2.5'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='4.2.5'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='4.2.5'),
          @Grab(group='net.sourceforge.owlapi', module='org.semanticweb.hermit', version='1.3.8.413'),
          @Grab(group='org.semanticweb.elk', module='elk-owlapi', version='0.4.2'),
          @GrabConfig(systemClassLoader=true)
        ])

import org.semanticweb.owlapi.model.parameters.*
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.reasoner.*
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.owllink.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.search.*;
import org.semanticweb.owlapi.manchestersyntax.renderer.*;
import org.semanticweb.owlapi.reasoner.structural.*
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import java.io.File;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerConfiguration
import org.semanticweb.elk.reasoner.config.*


OWLOntologyManager manager = OWLManager.createOWLOntologyManager()
OWLOntology in_full_ontology = manager.loadOntologyFromOntologyDocument(new File("HPC_MPC_with_empty_collections_fixed_absence_removed_chebi.owl"))
OWLOntology outont_lite = manager.createOntology(IRI.create("http://aber-owl.net/inferred-ontology.owl"))
OWLDataFactory fac = manager.getOWLDataFactory()
ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)



println("Adding infrerences from MPC using a DL reasoner")

OWLOntology in_ont = manager.loadOntologyFromOntologyDocument(new File("MPC-d_with_empty_collections_fixed_absence_removed_chebi_koncluded.owl"))


println("Start adding axioms related to new classes")
in_ont.getTBoxAxioms(Imports.INCLUDED).each {
 ax ->
 str = ax.toString()
    println(str)
    if(str.indexOf("CCL")>-1 || str.indexOf("MPC")>-1 || str.indexOf("CCLP")>-1 || str.indexOf("HPC")>-1 ){
       manager.addAxiom(outont_lite, ax)

    }
}


println("Adding infrerences from HPC using a DL reasoner")

in_ont = manager.loadOntologyFromOntologyDocument(new File("HPC-d_with_empty_collections_fixed_absence_removed_chebi_koncluded.owl"))


println("Start adding axioms related to new classes")
in_ont.getTBoxAxioms(Imports.INCLUDED).each {
 ax ->
 str = ax.toString()
    println(str)
    if(str.indexOf("CCL")>-1 || str.indexOf("MPC")>-1 || str.indexOf("CCLP")>-1 || str.indexOf("HPC")>-1 ){
       manager.addAxiom(outont_lite, ax)

    }
}



println("Adding infrerences from mergedOnt using a ELK reasoner")

ElkReasonerFactory f1 = new ElkReasonerFactory()
OWLReasoner reasoner = f1.createReasoner(in_full_ontology,config)

reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)
InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredEquivalentClassAxiomGenerator()])
generator.fillOntology(fac, outont_lite)



manager.saveOntology(outont_lite, IRI.create(new File("merged_reasoned_HPC_MPC_fixed_absence_removed_chebi_ELK_konclude_light.owl").toURI()))
