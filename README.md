# CardinalityPhenotypes
Phenotype data is critical for deciphering the biological mechanisms causing a disease. A formal ontological description of phenotype data can assist in identifying and interpreting these mechanisms. Many ontologies cover the domain of phenotypes for specific organisms, such as the Human Phenotype Ontology (HP) and the Mammalian Phenotype Ontology (MP). Most phenotype ontologies define phenotypes using the Entity--Quality (EQ) formalism

Here, we are interested in the phenotype ontology axioms related to an increased or decreased amount of entities present within a body. For
example, "decreased T cell number" can be defined using the EQ model as equivalent to 


"has_part" some ('decreased amount' and ('characteristic of' some 'T cell') and ('has modifier' some abnormal))}

"decreased lymphocyte cell number" defined as "has\_part" some ('decreased amount' and ('characteristic of' some  lymphocyte) and ('has modifier' some abnormal))}.  Based on these definitions, "decreased T cell number" is inferred to be a subclass of {\em decreased lymphocyte cell number }. However, depending on the specific meaning of {\em decreased T cell number} and
"decreased lymphocyte cell number", this may be an unintended inference: if {\em decreased T cell number} and "decreased lymphocyte cell number" refer to "all" T cells and lymphocyte
within a body, then the inference of the subclass axiom is not correct because the decrease or increase of the number of T cells does not imply the decrease/increase of the number of lymphocytes in a body. Similar issues arise when formalizing the absence of T cells, as the absence of T cells does not usually imply the absence of lymphocyte. We identify 2,341 such cases in the MP and 1,119 in the HPO. Among those classes, 490 MP classes and 57 HP classes refer to increased or decreased cell types. The underlying problem here is that cardinality phenotypes use in their definitions a class that has individual entities as instances whereas single entities (such as a T cell) should not be counted; instead, cardinality is a quality of a collection of entities. We rely on the work of [ref](https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=96c9d09bc7e6d32fe246a84bf51ebc4466bb7f51#page=121) which previously analyzed collections in formal ontologies.





We explicitly introduce "collections" of anatomical entities; in particular, we introduce "maximal collections" of entities with respect to another entity, which we define as the collection of all entities of a particular type that are (spatially) contained within another entity. For example, in addition to the class "T cell" which has individual T cells as instances, we introduce a class for the collection of all T cells within a body. Based on collection classes, we can formulate a design pattern that represents the cardinality phenotype, where the entity in the EQ method is replaced with the collection of entity classes that we have formulated. Restructuring MP and HPO using our ontology design pattern improves the identification of gene-disease association through semantic similarity.



[The ontology file](https://github.com/bio-ontology-research-group/CardinalityPhenotypes/blob/main/Minimal_working_ontology/CardinalityPhenotype.owl) represents a minimal working ontology, with collections and collections phenotypes. 



We applied these new representations to MP and HP. We need both ontologies in the same classification hierarchy. As the reasoning and classification task is challenging, we came up with an approach of generating each ontology and classifying them using [Konclude](https://github.com/konclude/Konclude). The code used to generate the ontologies is located [here](https://github.com/bio-ontology-research-group/CardinalityPhenotypes/blob/main/Create_Merged_Ontology/fixCardinalityCellurarPhenotypes_MP_HP.groovy). After that we only added the infred axiomed between the newly generated classes using the script [here](https://github.com/bio-ontology-research-group/CardinalityPhenotypes/blob/main/Create_Merged_Ontology/MakeInferredOntology_mix_Elk_Konclude_on_singular.groovy). The resulted ontology is then used for gene-disease prediction based on phenotypic similarity.
