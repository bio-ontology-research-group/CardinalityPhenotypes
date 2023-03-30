
@Grapes([
          @Grab(group='org.semanticweb.elk', module='elk-owlapi', version='0.4.2'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-api', version='4.2.6'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-apibinding', version='4.2.6'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-impl', version='4.2.6'),
          @Grab(group='net.sourceforge.owlapi', module='owlapi-parsers', version='4.2.6'),
          @Grab(group='edu.stanford.protege', module='org.protege.editor.owl', version='4.3.0'),
          @Grab(group='org.slf4j', module='slf4j-simple', version='1.6.1'),
@Grab(group='net.sourceforge.owlapi', module='org.semanticweb.hermit', version='1.3.8.413')
])

import org.semanticweb.owlapi.model.parameters.*
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerConfiguration
import org.semanticweb.elk.reasoner.config.*
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
import org.semanticweb.owlapi.manchestersyntax.parser.*
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.parser.ParserUtil;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;
import java.util.function.Supplier
import org.semanticweb.owlapi.expression.OWLEntityChecker
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.HermiT.ReasonerFactory;


import java.util.regex.Pattern



OWLOntologyManager manager = OWLManager.createOWLOntologyManager()
OWLDataFactory fac = manager.getOWLDataFactory()

ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor()
OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor)
StructuralReasonerFactory f = new StructuralReasonerFactory()

OWLOntology mpcOnt = manager.loadOntologyFromOntologyDocument(new File("../../mp.owl"))
OWLOntology hpcOnt = manager.loadOntologyFromOntologyDocument(new File("../../hp.owl"))

Set<OWLOntology> onts = [mpcOnt , hpcOnt]


def mperged = IRI.create("http://ontology_url/")


OWLOntology mergedOnt = manager.createOntology(mperged,onts)
OWLReasoner reasoner = f.createReasoner(mergedOnt,config)


boolean isDeprecated(cl,outont) {
  deprecated = false 
  EntitySearcher.getAnnotationAssertionAxioms(cl, outont).each { ax ->
      if (ax.isDeprecatedIRIAssertion()) {
        OWLLiteral value = ax.getValue().asLiteral().orNull();
        if (value != null && value.parseBoolean()) {
          deprecated = true;
        }
      }
    }
  if(deprecated){
    println("deprecated"+cl.toString())
  }
  return deprecated
}




def onturi = "http://purl.obolibrary.org/obo/"




String formatClassNames(String s) {
  s=s.replace("<http://purl.obolibrary.org/obo/","")
  s=s.replace(">","")
  s=s.replace("_",":")
  s
}
def id2name = [:]
mergedOnt.getClassesInSignature(true).each { cl ->
  EntitySearcher.getAnnotationObjects(cl, mergedOnt, fac.getRDFSLabel()).each { lab ->
    if (lab.getValue() instanceof OWLLiteral) {
      def labs = (OWLLiteral) lab.getValue()
      id2name[cl] = labs.getLiteral()
    }
  }
}
def id2class = [:] // maps a name to an OWLClass
mergedOnt.getClassesInSignature(true).each {
  def aa = it.toString()
  aa = formatClassNames(aa)
  if (id2class[aa] != null) {
  } else {
    id2class[aa] = it
  }
}

def class2id = [:] // maps a name to an OWLClass
mergedOnt.getClassesInSignature(true).each {
  def aa = it.toString()
  aa = formatClassNames(aa)
  if (class2id[it] != null) {
  } else {
    class2id[it] = aa
  }
}


def addAnno = {resource, prop, cont ->
  //  OWLAnnotation anno = fac.getOWLAnnotation(fac.getOWLAnnotationProperty(prop.getIRI()), fac.getOWLLiteral(cont))
  def axiom = fac.getOWLAnnotationAssertionAxiom(fac.getOWLAnnotationProperty(prop.getIRI()), resource.getIRI(), cont)
  manager.addAxiom(mergedOnt ,axiom)
}

def R = { String s ->
  if (s == "part-of") {
    fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"))
  } else if (s == "has-part") {
    fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000051"))
  } else {
    fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/#"+s))
  }
}

def C = { String s ->
  fac.getOWLClass(IRI.create(onturi+s))
}

def and = { cl1, cl2 ->
  fac.getOWLObjectIntersectionOf(cl1,cl2)
}
def some = { r, cl ->
  fac.getOWLObjectSomeValuesFrom(r,cl)
}
def equiv = { cl1, cl2 ->
  fac.getOWLEquivalentClassesAxiom(cl1, cl2)
}
def subclass = { cl1, cl2 ->
  fac.getOWLSubClassOfAxiom(cl1, cl2)
}



println "Find amount related patterns..."

println("------------------------")

classes_to_be_fixed = []

println("find classes with amounts in their descreption")

  amount = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000070"))
  amount_classes = reasoner.getSubClasses(amount, false).getFlattened()
  amount_classes.add(amount)
  //print(amount_classes)

  mergedOnt.getClassesInSignature(true).each { cl ->
    if (cl.toString().indexOf("MP_")>-1 || cl.toString().indexOf("HP_")>-1 ) {    
    EntitySearcher.getEquivalentClasses(cl, mergedOnt).each { cExpr -> // OWL Class Expression
      amount_classes.each{ a->
        if (cExpr.toString().indexOf(a.toString())>-1 && cExpr.toString().indexOf("CL_")>-1 && cExpr.toString().indexOf("CHEBI")==-1){


          classes_to_be_fixed.add(class2id[cl])
          println(class2id[cl])

        }

      }
    }
  }
}

absence_classes_to_be_fixed = []

  absent = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000462"))
  absent_classes = reasoner.getSubClasses(absent, false).getFlattened()
  absent_classes.add(absent)
  println(absent_classes)

  mergedOnt.getClassesInSignature(true).each { cl ->
    if (cl.toString().indexOf("MP_")>-1 || cl.toString().indexOf("HP_")>-1) {    
    EntitySearcher.getEquivalentClasses(cl, mergedOnt).each { cExpr -> // OWL Class Expression
      absent_classes.each{ a->
        if (cExpr.toString().indexOf(a.toString())>-1 && cExpr.toString().indexOf("CL_")>-1 && cExpr.toString().indexOf("CHEBI")==-1){

          absence_classes_to_be_fixed.add(class2id[cl])
          classes_to_be_fixed.add(class2id[cl])
          println(class2id[cl])


        }

      }
    }
  }
}





//-------------------------------------------------
println("creating the new ontology")

mpc_root = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/MPC_0000000"))
label = fac.getOWLAnnotation(fac.getRDFSLabel(),fac.getOWLLiteral( "phenotypic abnormality of collection of cell"))
axiom = fac.getOWLAnnotationAssertionAxiom(mpc_root.getIRI(), label)
manager.applyChange(new AddAxiom(mergedOnt, axiom))
manager.applyChange(new AddAxiom(mpcOnt, axiom))
mp_root = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/MP_0000001"))
manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(mpc_root,mp_root)) 
manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(mpc_root,mp_root)) 


hpc_root = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/HPC_0000000"))
label = fac.getOWLAnnotation(fac.getRDFSLabel(),fac.getOWLLiteral( "phenotypic abnormality of collection of cell"))
axiom = fac.getOWLAnnotationAssertionAxiom(hpc_root.getIRI(), label)
manager.applyChange(new AddAxiom(mergedOnt, axiom))
manager.applyChange(new AddAxiom(hpcOnt, axiom))
hp_root = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/HP_0000001"))
manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(hpc_root,hp_root)) 
manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(hpc_root,hp_root)) 


classes_to_be_fixed.each{ id_ ->
  if(id_.indexOf("MP:")>-1){
    def mpc_class = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/MPC_"+id_.replaceAll('MP:',''))) 
    label = fac.getOWLAnnotation(fac.getRDFSLabel(),fac.getOWLLiteral( id2name[id2class[id_]] + " for collection"))
    axiom = fac.getOWLAnnotationAssertionAxiom(mpc_class.getIRI(), label)
    manager.applyChange(new AddAxiom(mergedOnt, axiom))
    manager.applyChange(new AddAxiom(mpcOnt, axiom))
  }else if(id_.indexOf("HP:")>-1){
    def hpc_class = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/HPC_"+id_.replaceAll('HP:',''))) 
    label = fac.getOWLAnnotation(fac.getRDFSLabel(),fac.getOWLLiteral( id2name[id2class[id_]] + " for collection"))
    axiom = fac.getOWLAnnotationAssertionAxiom(hpc_class.getIRI(), label)
    manager.applyChange(new AddAxiom(mergedOnt, axiom))
    manager.applyChange(new AddAxiom(hpcOnt, axiom))
  }
  
}


axioms_to_be_added = [:]
has_member = fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002351"))
has_modifier = fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002573"))
phenotype_of = fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002201"))
abnormal = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000460"))
has_modifier_abnormal = fac.getOWLObjectSomeValuesFrom(has_modifier,abnormal)
has_part = fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000051"))
part_of = fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000050"))
//inheres_in =  fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/BFO_0000052"))
characteristic_of =  fac.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0000052"))

SimpleShortFormProvider shortFormProvider = new SimpleShortFormProvider(); 
ManchesterOWLSyntaxOWLObjectRendererImpl rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl()
rendering.setShortFormProvider(shortFormProvider)
Set<OWLOntology> importsClosure = mergedOnt.getImportsClosure();
mapper = new BidirectionalShortFormProviderAdapter(manager, importsClosure, shortFormProvider);
mapper.add(fac.getOWLThing())
mapper.add(fac.getOWLNothing())
mapper.add(phenotype_of)
OWLEntityChecker eChecker = new ShortFormEntityChecker(mapper);
ManchesterOWLSyntaxClassExpressionParser parser = new ManchesterOWLSyntaxClassExpressionParser(fac, eChecker);


// should inheres_in and characteristic_of be equivilant ?!

//classes rendering to be used 
ls_amount_renders = []
amount_classes.each{cl ->
 render_id =  rendering.render(cl)
 //println(render_id)
 ls_amount_renders.add(render_id)
}

owlthing_render = rendering.render(fac.getOWLClass(IRI.create("http://www.w3.org/2002/07/owl#Thing")))
//println(eChecker.getOWLClass(owlthing_render).toString())
//println("test parsing " + owlthing_render)
//println(parser.parse(owlthing_render))
//fac.getOWLThing().toString()
//rendering.render(fac.getOWLThing())

  
//counter for hierarchal classes (CCLP) Collection of Cells Phenotupes
counter = 0
done_cclp = [:]
ccl_classes = []
ccl_root = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CCL_0000000x"))

mergedOnt.getTBoxAxioms(Imports.INCLUDED).each { ax ->
  need_to_check = false
  classes_to_be_fixed.find{cl ->
    if(ax.toString().indexOf(cl.replaceAll(":",'_'))>-1){
        need_to_check=true
    }
  }

  // if we don't need original classes things need to be changed here ...
  manager.addAxiom(mergedOnt, ax)
  if(need_to_check){
    // check if axiom is an equeivilant class axiom (class description that we need to change )
    if(ax.getAxiomType()==AxiomType.EQUIVALENT_CLASSES){
      equiv_ls = ax.getClassExpressionsAsList()
      if(equiv_ls.size>1){
        class_ = equiv_ls[0]           
        description = equiv_ls[1]

        if(classes_to_be_fixed.contains(class2id[class_]) ){

          if (description.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {

            //replace cell class by CCL_ class (collection of cell) and replace MP class by MPC_
            text = rendering.render(description)
            //println(text)
            if(class_.toString().indexOf("MP")>-1){

                mpc_class = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/MPC_"+class2id[class_].replaceAll('MP:','')))
                axioms_to_be_added[mpc_class]=text
                rendering.render(description).findAll(/CL_(\d*)/){ c->
                //println (c)
                ccl_class = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/C"+c[0]))
                ccl_classes.add(c[0])
                mapper.add(ccl_class)
                label = fac.getOWLAnnotation(fac.getRDFSLabel(),fac.getOWLLiteral( "collection of "+ id2name[id2class[c[0].replace("_",":")]] ))
                OWLAxiom axiom = fac.getOWLAnnotationAssertionAxiom(ccl_class.getIRI(), label)
                manager.applyChange(new AddAxiom(mergedOnt, axiom))
                manager.applyChange(new AddAxiom(mpcOnt, axiom))
                //manager.applyChange(new AddAxiom(hpcOnt, axiom))
                manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(ccl_class,fac.getOWLObjectAllValuesFrom(has_member, id2class[c[0].replace("_",":")])))
                manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(ccl_class,fac.getOWLObjectAllValuesFrom(has_member, id2class[c[0].replace("_",":")])))
                //manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(ccl_class,fac.getOWLObjectAllValuesFrom(has_member, id2class[c[0].replace("_",":")])))

                if(!done_cclp.containsKey(c[0])){
                  cclp = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CCLP_"+counter.toString().padLeft(7, '0')))
                  discription_sameclass =   text.replaceAll("CL_","CCL_") 
                  ls_amount_renders.each{ar->
                      discription=discription_sameclass.replaceAll(ar,'PATO_0000001')
                      discription_sameclass = discription
                  }
                  //println(discription)
                  new_class_discription=parser.parse(discription_sameclass)
                  //println()
                  //println ("adding "+ c[0] + " CCLP:"+counter.toString().padLeft(7, '0') )
                  manager.addAxiom(mergedOnt, fac.getOWLEquivalentClassesAxiom(cclp,new_class_discription))
                  manager.addAxiom(mpcOnt, fac.getOWLEquivalentClassesAxiom(cclp,new_class_discription))
                  //manager.addAxiom(hpcOnt, fac.getOWLEquivalentClassesAxiom(cclp,new_class_discription))
                  label = fac.getOWLAnnotation(fac.getRDFSLabel(),fac.getOWLLiteral( "collection of "+ id2name[id2class[c[0].replace("_",":")]]+" phenotype"))
                  axiom = fac.getOWLAnnotationAssertionAxiom(cclp.getIRI(), label)
                  manager.applyChange(new AddAxiom(mergedOnt, axiom))
                  //manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(mpc_class,cclp))
                  manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(cclp,mpc_root))
                  manager.applyChange(new AddAxiom(mpcOnt, axiom))
                  //manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(mpc_class,cclp))
                  manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(cclp,mpc_root))
                  manager.applyChange(new AddAxiom(hpcOnt, axiom))
                  //manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(cclp,hpc_root))
                  done_cclp[c[0]]=cclp

                  counter++
                  }else{
                    //manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(mpc_class,done_cclp[c[0]]))
                    //manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(mpc_class,done_cclp[c[0]]))
                    println("")
                  }
                   manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(ccl_class,ccl_root))
                   manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(ccl_class,ccl_root))
                   manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(ccl_class,ccl_root))
                 }

              }

              if(class_.toString().indexOf("HP")>-1){
                hpc_class = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/HPC_"+class2id[class_].replaceAll('HP:','')))
                axioms_to_be_added[hpc_class]=text
                rendering.render(description).findAll(/CL_(\d*)/){ c->
                //println (c)

                  ccl_class = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/C"+c[0]))
                  ccl_classes.add(c[0])
                  mapper.add(ccl_class)
                  label = fac.getOWLAnnotation(fac.getRDFSLabel(),fac.getOWLLiteral( "collection of "+ id2name[id2class[c[0].replace("_",":")]] ))
                  OWLAxiom axiom = fac.getOWLAnnotationAssertionAxiom(ccl_class.getIRI(), label)
                  manager.applyChange(new AddAxiom(mergedOnt, axiom))
                  manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(ccl_class,fac.getOWLObjectAllValuesFrom(has_member, id2class[c[0].replace("_",":")])))
                  //manager.applyChange(new AddAxiom(mpcOnt, axiom))
                  //manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(ccl_class,fac.getOWLObjectAllValuesFrom(has_member, id2class[c[0].replace("_",":")])))
                  manager.applyChange(new AddAxiom(hpcOnt, axiom))
                  manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(ccl_class,fac.getOWLObjectAllValuesFrom(has_member, id2class[c[0].replace("_",":")])))

                  if(!done_cclp.containsKey(c[0])){
                    cclp = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CCLP_"+counter.toString().padLeft(7, '0')))
                    discription_sameclass =  text.replace("CL_","CCL_") 
                    ls_amount_renders.each{ar->
                      discription=discription_sameclass.replaceAll(ar,'PATO_0000001')
                      discription_sameclass = discription
                    }
                    //println(discription)
                    new_class_discription=parser.parse(discription_sameclass)
                    //println ("adding "+ c[0] + " CCLP:"+counter.toString().padLeft(7, '0') )
                    manager.addAxiom(mergedOnt, fac.getOWLEquivalentClassesAxiom(cclp,new_class_discription))
                    //manager.addAxiom(mpcOnt, fac.getOWLEquivalentClassesAxiom(cclp,new_class_discription))
                    manager.addAxiom(hpcOnt, fac.getOWLEquivalentClassesAxiom(cclp,new_class_discription))
                    label = fac.getOWLAnnotation(fac.getRDFSLabel(),fac.getOWLLiteral( "collection of "+ id2name[id2class[c[0].replace("_",":")]]+" phenotype"))
                    axiom = fac.getOWLAnnotationAssertionAxiom(cclp.getIRI(), label)
                    manager.applyChange(new AddAxiom(mergedOnt, axiom))
                    //manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(hpc_class,cclp))
                    manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(cclp,hpc_root))
                    manager.applyChange(new AddAxiom(hpcOnt, axiom))
                    //manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(hpc_class,cclp))
                    manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(cclp,hpc_root))

                    done_cclp[c[0]]=cclp

                    counter++
                    }else{
                    //manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(hpc_class,done_cclp[c[0]]))
                    manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(hpc_class,done_cclp[c[0]]))

                  }
                   manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(ccl_class,ccl_root))
                   manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(ccl_class,ccl_root))
                   manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(ccl_class,ccl_root))
                 }
              }
          }else{
           println("not some values from  " +description)
          }
           
        }else{
          println("not class form the list " +class2id[class_])
        }

      }


    }//else it is a subclass axiom
  }
}


importsClosure = mergedOnt.getImportsClosure();
//mapper = new BidirectionalShortFormProviderAdapter(manager, importsClosure, shortFormProvider);
eChecker = new ShortFormEntityChecker(mapper);
parser = new ManchesterOWLSyntaxClassExpressionParser(fac, eChecker);


get_hierarchal = { class_description ->
  to_return = []
  //println(class_description)

  //def pattern = Pattern.compile(/(?ms)(BFO_0000051 some\n    \(PATO_)/)

   class_description.find(/PATO_(\d*)/){c->

    if(ls_amount_renders.contains(c[0])){
      println("found pato in amount "+ c[0])
      pato  = c[0].replace("_",":").replace("(","")

      label = "collection of cells "+id2name[id2class[pato]]
      descreption =  class_description 
      descreption.find(/CL_(\d*)/){x->
        temp = descreption.replace(x[0],"CCL_0000000x")
        descreption=temp

      }
      //descreption = "RO_0002201 some ("+ class_description.replace("CCL_.* ",owlthing_render) +")"
      println("check")
      println(descreption)

      to_return.add([descreption,label,pato])
    }
  }
  return to_return
}


println("The Classes to be added---")
println(axioms_to_be_added)




absents_count = 0
done_pato = [:]



absens_of_a_collection_of_cells = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CCLP_"+counter.toString().padLeft(7, '0')))
label = fac.getOWLAnnotation(fac.getRDFSLabel(),fac.getOWLLiteral( "absence of a collection of cells" ))
axiom = fac.getOWLAnnotationAssertionAxiom(absens_of_a_collection_of_cells.getIRI(), label)
manager.applyChange(new AddAxiom(mergedOnt, axiom))
manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(absens_of_a_collection_of_cells,mpc_root))

manager.applyChange(new AddAxiom(mpcOnt, axiom))
manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(absens_of_a_collection_of_cells,mpc_root))

manager.applyChange(new AddAxiom(hpcOnt, axiom))
manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(absens_of_a_collection_of_cells,mpc_root))

counter++

axioms_to_be_added.each{ hmpc, class_description ->


  hmp_id = formatClassNames(hmpc.toString()).replace("MPC:","MP:").replace("HPC:","HP:")
  if(absence_classes_to_be_fixed.contains(hmp_id)){

    class_description.findAll(/CL_(\d*)/){ c->
              cl_class = c[0]

            }

    entity = cl_class
    descreption = " BFO_0000051 some ( PATO_0000001 and RO_0000052 some (C"+ entity +" and (RO_0002351 only Nothing )))"
    descreption2 = " RO_0002201 some (not ( BFO_0000051 some ( PATO_0000001 and ( RO_0000052 some "+ entity +" ) ) ) )"
    //descreption2 = " not ( BFO_0000051 some ( PATO_0000001 and ( RO_0000052 some "+ entity +" ) ) )"
    //println(("adding " + descreption))
    OWLClassExpression new_class_discription=parser.parse(descreption)
    OWLClassExpression new_class_discription2=parser.parse(descreption2)
    absents_count++
    manager.addAxiom(mergedOnt, fac.getOWLEquivalentClassesAxiom(hmpc,new_class_discription))
    manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(hmpc,absens_of_a_collection_of_cells))
    manager.addAxiom(mergedOnt, fac.getOWLEquivalentClassesAxiom(new_class_discription2,new_class_discription))

    if(hmp_id.indexOf("MP") > -1){
      manager.addAxiom(mpcOnt, fac.getOWLEquivalentClassesAxiom(hmpc,new_class_discription))
      manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(hmpc,absens_of_a_collection_of_cells))
      manager.addAxiom(mpcOnt, fac.getOWLEquivalentClassesAxiom(new_class_discription2,new_class_discription))
    }
    if(hmp_id.indexOf("HP") > -1){
      manager.addAxiom(hpcOnt, fac.getOWLEquivalentClassesAxiom(hmpc,new_class_discription))
      manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(hmpc,absens_of_a_collection_of_cells))
      manager.addAxiom(hpcOnt, fac.getOWLEquivalentClassesAxiom(new_class_discription2,new_class_discription))
    }


  }else{

      println( hmpc.toString() + "|||"+ class_description)
      class_description_fix =  class_description.replaceAll("CL_","CCL_")
      OWLClassExpression new_class_discription=parser.parse(class_description_fix)
      manager.addAxiom(mergedOnt, fac.getOWLEquivalentClassesAxiom(hmpc,new_class_discription))
      if(hmp_id.indexOf("MP") > -1){
        manager.addAxiom(mpcOnt, fac.getOWLEquivalentClassesAxiom(hmpc,new_class_discription))
      }
      if(hmp_id.indexOf("HP") > -1){
        manager.addAxiom(hpcOnt, fac.getOWLEquivalentClassesAxiom(hmpc,new_class_discription))
      }

      // add hierarchal classes CP_xx
      println("Trying to build hierarchal classes starts..")

      ls_of_hierarchal_classes = get_hierarchal(class_description)
      if(ls_of_hierarchal_classes.size > 0){
        ls_of_hierarchal_classes.each{ d,l,pato ->
           cp = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CCLP_"+counter.toString().padLeft(7, '0')))
           new_class_discription=parser.parse(d)
           equivalent_classes=reasoner.getEquivalentClasses(new_class_discription).getEntities()
           dont_add=false
           if(!done_pato.containsKey(pato)){
            reasoner = f.createReasoner(mergedOnt,config)
            equivalent_classes.each{ equivalent ->
                if(equivalent instanceof OWLClass && equivalent.toString().indexOf("/CCLP_")>-1){
                    dont_add =true
                }
              }
             if(!dont_add)
             {
                done_pato[pato]= cp
                //println ("adding "+ l + " CCLP:"+counter.toString().padLeft(7, '0') + " " +d)
                manager.addAxiom(mergedOnt, fac.getOWLEquivalentClassesAxiom(cp,new_class_discription))
                label = fac.getOWLAnnotation(fac.getRDFSLabel(),fac.getOWLLiteral( l ))
                axiom = fac.getOWLAnnotationAssertionAxiom(cp.getIRI(), label)
                manager.applyChange(new AddAxiom(mergedOnt, axiom))
                //manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(hmpc,cp))
                //manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(cp,hpc_root))
                //manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(cp,mpc_root))
                if(hmp_id.indexOf("MP") > -1){
                  manager.addAxiom(mpcOnt, fac.getOWLEquivalentClassesAxiom(cp,new_class_discription))
                  manager.applyChange(new AddAxiom(mpcOnt, axiom))
                }
                //manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(hmpc,cp))
                //manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(cp,hpc_root))
                //manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(cp,mpc_root))

                if(hmp_id.indexOf("HP") > -1){
                  manager.addAxiom(hpcOnt, fac.getOWLEquivalentClassesAxiom(cp,new_class_discription))
                  manager.applyChange(new AddAxiom(hpcOnt, axiom))
                }
                //manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(hmpc,cp))
                //manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(cp,hpc_root))
                //manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(cp,mpc_root))

                counter++
             }

           //}else{
            //manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(hmpc,done_pato[pato]))
            //manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(hmpc,done_pato[pato]))
            //manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(hmpc,done_pato[pato]))
           //}
           }
        }
      }

  }
}



// adding collections parthood relation ( A subclass of B ) -> ( collection of A is part of collection of B )
//-----------------------------------------------------------------------------------------------------------

reasoner = f.createReasoner(mergedOnt,config)
ccl_classes.toSet().each{
  cl_id ->
  //println(fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+cl_id)))
    reasoner.getSuperClasses(fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/"+cl_id)),true).getFlattened().each(){
      cl_super ->
      start =  cl_super.toString().lastIndexOf("/")+1
      end = cl_super.toString().length() -1
      super_id = cl_super.toString().substring(start,end)
      println(super_id)
      if (ccl_classes.contains(super_id) ){
        ccl = fac.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/C"+cl_id))
        manager.addAxiom(mergedOnt, fac.getOWLSubClassOfAxiom(ccl,fac.getOWLObjectSomeValuesFrom(part_of,cl_super) ) )
        manager.addAxiom(mpcOnt, fac.getOWLSubClassOfAxiom(ccl,fac.getOWLObjectSomeValuesFrom(part_of,cl_super) ) )
        manager.addAxiom(hpcOnt, fac.getOWLSubClassOfAxiom(ccl,fac.getOWLObjectSomeValuesFrom(part_of,cl_super) ) )
      }
    }
}


      

classes = mergedOnt.getClassesInSignature(true)

new File("/home/alghsm0a/ICD_mapping/hp2mp_fromOBO.txt").splitEachLine("\t") {
 line ->
  if (!line[0].startsWith("#") && !line[0].startsWith("Source")) {
   def iri1 = fac.getOWLClass(IRI.create(line[0]))
   def iri2 = fac.getOWLClass(IRI.create(line[1]))
   def iri1C = fac.getOWLClass(IRI.create(line[0].replace("HP_","HPC_")))
   def iri2C = fac.getOWLClass(IRI.create(line[1].replace("MP_","MPC_")))
   def score = new Double(line[2])
   if (score >= 0.60) {
    if(!isDeprecated(iri1,mergedOnt) && !isDeprecated(iri2,mergedOnt))
    {
    manager.addAxiom(mergedOnt, equiv(iri1, iri2))
    manager.addAxiom(mpcOnt, equiv(iri1, iri2))
    manager.addAxiom(hpcOnt, equiv(iri1, iri2))
    if(classes.contains(iri1C) && classes.contains(iri2C))
      {manager.addAxiom(mergedOnt, equiv(iri1C, iri2C))
      manager.addAxiom(mpcOnt, equiv(iri1C, iri2C))
      manager.addAxiom(hpcOnt, equiv(iri1C, iri2C))}
    }
   }
  }
}



manager.saveOntology(mergedOnt, new OWLXMLDocumentFormat(),IRI.create((new File("HPC_MPC_with_empty_collections_fixed_absence_removed_chebi.owl").toURI())))
manager.saveOntology(mpcOnt,  new OWLXMLDocumentFormat(),IRI.create((new File("MPC-d_with_empty_collections_fixed_absence_removed_chebi.owl").toURI())))
manager.saveOntology(hpcOnt,  new OWLXMLDocumentFormat(),IRI.create((new File("HPC-d_with_empty_collections_fixed_absence_removed_chebi.owl").toURI())))





/*
//This is the reasonong task. works only when tested with mini MP and mini HP 
//reasoing for DL-axioms related to MPC,HPC,CCL, CCLP classes ..

println("Adding Elk inferences")

rf = new ElkReasonerFactory()
reasoner = rf.createReasoner(mergedOnt,config)
reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY)
InferredOntologyGenerator generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredEquivalentClassAxiomGenerator()])
generator.fillOntology(fac, mergedOnt)

println("Adding infrerences from MPC using a DL reasoner")

hf = new ReasonerFactory()
reasoner = hf.createReasoner(mpcOnt,config)
generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredEquivalentClassAxiomGenerator()])
axiom_types = generator.getAxiomGenerators()
axiom_types.each{type->
  all_axioms = type.createAxioms(fac, reasoner) 
  all_axioms.each{ ax->
    str = ax.toString()
    println(str)
    if(str.indexOf("CCL")>-1 || str.indexOf("MPC")>-1 || str.indexOf("CCLP")>-1 || str.indexOf("HPC")>-1 ){
       manager.addAxiom(mergedOnt, ax)

    }
  }

} 

println("Adding infrerences from HPC using a DL reasoner")

reasoner = hf.createReasoner(hpcOnt,config)
generator = new InferredOntologyGenerator(reasoner, [new InferredSubClassAxiomGenerator(), new InferredEquivalentClassAxiomGenerator()])
axiom_types = generator.getAxiomGenerators()
axiom_types.each{type->
  all_axioms = type.createAxioms(fac, reasoner) 
  all_axioms.each{ ax->
    str = ax.toString()
    println(str)
    if(str.indexOf("CCL")>-1 || str.indexOf("MPC")>-1 || str.indexOf("CCLP")>-1 || str.indexOf("HPC")>-1 ){
       manager.addAxiom(mergedOnt, ax)

    }
  }

} 





manager.saveOntology(mergedOnt, IRI.create(new File(new File("HPC_MPC_with_elk_and_hermit_fixed_absence_removed_chebi_wop.owl")).toURI()))

*/