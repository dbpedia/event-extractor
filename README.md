# event-extractor

## Introduction

Repository for the DBpedia GSoC Hybrid Classifier/Rule-based Event Extractor Project

This git was solely used by the student Vincent Bohlen (@s0taka). All commits were made by me. The code and the [weekly reports](https://github.com/dbpedia/event-extractor/wiki/Weekly-Reports) document the work I have done.

The project consists of two major parts: The MainWorkflow class, which coordinates training, classifying, etc. and the LearningWorkflow, which coordinates preprocessing and learning of the model.
Furthermore the project consists of a Crawler for gathering training material from wikipedia and a lot of annotation
, preprocessing and helper classes.

A demo is available via: http://dbpedia.imp.fu-berlin.de:32811/EventClassifier/demo/index.html

See Example texts for easy testing.

(The computation is slow because of Apache Spark overhead and the slow semViz instance used.)

## How To Use The Code

* Clone the repository

* Extract model and/or training material

* Change /src/main/resources so that modelPath and/or trainExamplesPath match you local path

* The main class is in MainWorklow.java

* main class trains the model and classifies one example.

* comment/uncomment the train() statement to only classify or train and classify

## Examples texts:

* A magnitude 6 earthquake struck off the east coast of Japan in the early hours of Sunday, the US Geological Survey reported. The quake’s epicentre was offshore, 192km (120 miles) east of Hachinohe, Honshu island at a depth of 10km.

* At least 13 people died in the flooding that swept through parts of southern Louisiana after torrential rains lashed the region. An estimated 60,000 homes have been damaged, and 102,000 people have registered for federal assistance so far.

* At least 96 homes and more than 200 other structures—sheds, barns and garages—have been destroyed by a large wildfire east of Los Angeles, fire officials said on Friday. The so-called Blue Cut fire was still churning through the high desert and mountain communities of southern California on Friday, though firefighters made some progress overnight, helped by cooler weather and higher humidity. The 37,000-acre fire is now 26% contained, fire officials said.

* The preliminary, unofficial storm reports indicate six locations of tornado touchdowns on August 20, 2016.
One persistent thunderstorm produced five of the tornado touchdowns. The first tornado touchdown took place at 1:15 p.m. at Bangor in Van Buren County.

* Six people injured in area traffic accidents.

* A minibus was hit by a train and killed four people in Anenii Noi District, Moldova.

* Three men face first looting charges in Milwaukee riots for 'breaking into liquor store'.

* Several police officers and at least one observer have been injured in violent protests in Milwaukee since Saturday night, when 23-year-old Sylville Smith was killed by police.

## Future Work: 

* The model needs further training. Right now it is only able to classify the five Events: Tornado, Flood, Wildfire Traffic, Riot and Earthquake. This is due to the sparsity of other natural disaster categories on Wikipedia. 

* Implementation of hierarchical classification may further improve precision.

* The classifier has no access to possibilities and will therefore show a result, even though no event is present. Need to find a way to omit showing results if possibility falls below a threshold. Maybe by accessing the models directly via Spark ML's OneVsRest class?

* The system should be migrated to a real spark cluster to reduce overhead.

* Is it possible to use the model without spark?

## Problems and Work Conclusion:
* I developed a system for event classification, supported by semantic frames and named entities

* Using Named Entities and Semantic Frames showed to be not as effective as hoped.

* Therefore I refrained from implementing the rule-based system which was planned to work with those

* Wikipedia's event category tunred out to be a bad place to (automatically) gather enough trainng material.

* The Events my system is able to classify are therefore sparse. 

* Given enough training examples the system is able to classify events pretty well