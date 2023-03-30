# This code is written by Sarah Alghamdi to generate the data for evaluatingf the representaions of cardinality phenotypes
#-----------------------------------------------------
# Sarah M. Alghamdi
#-----------------------------------------------------

import sys
import json

amount_of_cells = set([])

with open("collection_phenotype_classes.txt") as f:
    lines = f.readlines()
for line in lines:
    amount_of_cells.add(line.strip())


# get the genes with the cardinality phenotypes
diseases = set([])
human_genes = set([])
omim2hg = {}
omim2hp = {}
mgi2mp = {}
mouse_human_orthology = {}
positives = {}


with open("MGI_DO.rpt") as f:
    lines = f.readlines()
for line in lines:
	ls = line.split("\t")
	
	if(ls[3]=="human"):
		human_gene = ls[6].strip()
		omim_disease = ls[2].strip().split("|")
		#print(human_gene, omim_disease)

		if(human_gene is not "" and len(omim_disease)>0):
			human_genes.add(human_gene)

			for om in omim_disease:
				if (not om in omim2hg):
					omim2hg[om] = []
					diseases.add(om)
				omim2hg[om].append(human_gene)
				diseases.add(om)



with open("HMD_HumanPhenotype.rpt") as f:
    lines = f.readlines()
for line in lines:
	ls = line.split("\t")
	mgi = ls[3].strip()
	hg = ls[1].strip()
	
	if(hg not in mouse_human_orthology):
		mouse_human_orthology[hg] = []
	if(mgi not in mouse_human_orthology):
		mouse_human_orthology[mgi] = []
	mouse_human_orthology[hg].append(mgi)
	mouse_human_orthology[mgi].append(hg)


included_human_genes = set([])
with open("MGI_PhenoGenoMP.rpt") as f:
    lines = f.readlines()
for line in lines:
	ls = line.split("\t")
	
	mgi = ls[5].strip()
	mp = ls[3].strip()

	if(mp in amount_of_cells):		
		genes = mgi.split("|")
		for mg in genes:
			if mg in mouse_human_orthology:
				for hg in mouse_human_orthology[mg]:
					included_human_genes.add(hg)
			if mg not in mgi2mp:
				mgi2mp[mg] = []
			if (mp not in mgi2mp[mg]):
				mgi2mp[mg].append(mp.replace("MP:","MPC:"))


with open("phenotype_to_genes.txt") as f:
    lines = f.readlines()
for line in lines[1:]:
	ls = line.split("\t")
	hp = ls[0].strip()
	omim = ls[6].strip()
	hpc = hp.replace("HP:","HPC:")

	if(hp in amount_of_cells):	
		if("OMIM" in omim):	
			if omim not in omim2hp:
				omim2hp[omim]=[]
			omim2hp[omim].append(hpc)	




for omim in omim2hg:
	if omim in omim2hp:
		for hg in omim2hg[omim]:
			if hg in mouse_human_orthology:
				for mg in mouse_human_orthology[hg]:
					if mg in mgi2mp:
						if (omim not in positives):
							positives[omim] = []
						positives[omim].append(mg)

#print(amount_of_cells)
print("--------------------------")
#print(diseases)
print("--------------------------")
#print(human_genes)
print("--------------------------")
#print(omim2hg)
print("--------------------------")
#print(omim2hp)
print("--------------------------")
#print(mgi2mp)
print("--------------------------")
#print(mouse_human_orthology)
print("--------------------------")
#print(positives)
print("--------------------------")


ls=set([])
for omim in omim2hg:
	for hg in omim2hg[omim]:
		ls.add((omim,hg))

print("number of associations", len(ls))
print("human genes ", len(list(set(human_genes))))
print("omim diseases ", len(list(set([x for x in diseases if 'OMIM' in x]))))


diseases_in_focus = [x for x in diseases if (x in omim2hp)]
ls = set([])
for omim in diseases_in_focus:
	for hg in omim2hg[omim]:
		ls.add((omim,hg))

print("omim diseases with cardinality phenotype ", len(list(set(diseases_in_focus))))
print("assosiations of diseases with cardinality phenotype ", len(list(ls)))
print("included human genes", len(included_human_genes))





'''

with open('omim2hp.json', 'w') as fb:
        json.dump(omim2hp,fb)

with open('mgi2mp.json', 'w') as fb:
        json.dump(mgi2mp,fb)

with open('positives.json', 'w') as fb:
        json.dump(positives,fb)




#------------------------------------------------------------------
# create Resnik input:
# output: 2 files, tab separated one for genes and one for diseases

disease_file = open("resnik_disease_hp_input.tsv", "w")
gene_file = open("resnik_gene_mp_input.tsv", "w")

for disease in omim2hp:
	line = disease
	for hp in omim2hp[disease]:
		line+="\t"+hp.replace("HPC:","http://purl.obolibrary.org/obo/HP_")
	disease_file.write(line+"\n")
disease_file.close()

for gene in mgi2mp:
	line = gene
	for mp in mgi2mp[gene]:
		line+="\t"+mp.replace("MPC:","http://purl.obolibrary.org/obo/MP_")
	gene_file.write(line+"\n")
gene_file.close()


disease_file = open("resnik_disease_hpc_input.tsv", "w")
gene_file = open("resnik_gene_mpc_input.tsv", "w")

for disease in omim2hp:
	line = disease
	for hp in omim2hp[disease]:
		line+="\t"+hp.replace("HPC:","http://purl.obolibrary.org/obo/HPC_")
	disease_file.write(line+"\n")
disease_file.close()

for gene in mgi2mp:
	line = gene
	for mp in mgi2mp[gene]:
		line+="\t"+mp.replace("MPC:","http://purl.obolibrary.org/obo/MPC_")
	gene_file.write(line+"\n")
gene_file.close()



#print(mouse_genes)

'''