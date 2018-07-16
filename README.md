The AIBench Project [![license](https://img.shields.io/badge/LICENSE-LGPLv3-blue.svg)]() 
========================

The AIBench Project is a Java application development framework focused on GUI-based scientific applications. More info at the [AIBench home page](http://www.aibench.org)

AIBench Team
------------
This project is an idea and is developed by the following members of the [SING Group](http://www.sing-group.org):

* Daniel Glez-Peña
* Florentino Fdez-Riverola

Other current/past contributors to the project are:

* Paulo Maia [University of Minho (Portugal)](http://www.uminho.pt).
* Miguel Rocha [University of Minho (Portugal)](http://www.uminho.pt).
* Miguel Reboiro-Jato [SING Group](http://www.sing-group.org).
* Hugo López Fernández [SING Group](http://www.sing-group.org).

Examples
--------
The following are examples of scientific applications developed on top of the AIBench framework:
* [LA-iMageS](http://www.la-images.net/).
* [Mass-Up](http://www.sing-group.org/mass-up/).
* [S2P](http://www.sing-group.org/s2p/).
* [OptFlux](http://www.optflux.org/).
* [OptFerm](http://darwin.di.uminho.pt/optferm/).
* [Biofilms Experiment Workbench](http://www.sing-group.org/bew/).
* [ADOPS](http://www.sing-group.org/ADOPS/).
* [Decision Peptide-Driven](http://www.sing-group.org/DPD/).
* [MLibrary](http://www.sing-group.org/MLibrary/).
* [PileLineGUI](http://www.sing-group.org/pileline/index.php/Main_Page).
* [BioAnnote](http://www.sing-group.org/bioannote/).
* [BioClass](http://www.sing-group.org/bioclass/).
* [@Note](http://sysbio.di.uminho.pt/anote/wiki/index.php/Main_Page).

Citing
------
If you are using AIBench, please, cite this publication:
> D. Glez-Peña; M. Reboiro-Jato; P. Maia; F. Díaz; F. Fdez-Riverola (2010) [AIBench: a rapid application development framework for translational research in biomedicine](http://dx.doi.org/10.1016/j.cmpb.2009.12.003). Computer Methods and Programs in Biomedicine 98(2010), pp. 191-203. ISSN: 0169-2607

Other publications
------------------
- H. López-Fernández; M. Reboiro-Jato; D. Glez-Peña; J.R. Méndez-Reboredo; H.M. Santos; R.J. Carreira; J.L. Capelo; F. Fdez-Riverola (2011) [Rapid development of proteomic applications with the AIBench framework](http://dx.doi.org/10.2390/biecoll-jib-2011-171). Journal of Integrative Bioinformatics. 8/3:171. ISSN: 1613-4516
- F. Fdez-Riverola; D. Glez-Peña; H. López-Fernández; M. Reboiro-Jato; J.R. Méndez (2012) [A Java application framework for scientific software development](http://dx.doi.org/10.1002/spe.1108). Software: Practice & Experience. Volume 42/8, pp. 1015-1036. ISSN: 0038-0644
- H. López-Fernández; M. Reboiro-Jato; J.A. Pérez-Rodríguez; F. Fdez-Riverola; D. Glez-Peña (2016) [The Artificial Intelligence Workbench: a retrospective review](http://dx.doi.org/10.14201/ADCAIJ2016517385). Advances in Distributed Computing and Artificial Intelligence Journal. Volume 5/1, pp. 73-85. ISSN: 2255-2863

Creating an AIBench application using the archetype 
----------------------------------------------------
Simply run the following command to create a new AIBench application using the Maven archetype:
```bash
mvn archetype:generate -DarchetypeGroupId=es.uvigo.ei.sing -DarchetypeArtifactId=aibench-archetype -DarchetypeVersion=2.10.2 -DgroupId=es.uvigo.ei.sing -DartifactId=my-aibench-application -DinteractiveMode=false
```
This command creates the new application under the folder `my-aibench-application`. You can select the version of the archetype (corresponding to the AIBench version) in `-DarchetypeVersion`.
