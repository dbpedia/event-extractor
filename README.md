# event-extractor

## Introduction

Repository for the DBpedia GSoC Hybrid Classifier/Rule-based Event Extractor Project

This git was solely used by the student Vincent Bohlen (@s0taka). All commits were made by me. The code and the [weekly reports](https://github.com/dbpedia/event-extractor/wiki/Weekly-Reports) document the work I have done.

The project consists of two major parts: The MainWorkflow class, which coordinates training, classifying, etc. and the LearningWorkflow, which coordinates preprocessing and learning of the model.
Furthermore the project consists of a Crawler for gathering training material from wikipedia and a lot of annotation
, preprocessing and helper classes.

A demo is available via: http://dbpedia.imp.fu-berlin.de:32811/EventClassifier/demo/index.html

See [Example texts](#example) for easy testing.

(The computation is slow because of Apache Spark overhead and the slow semViz instance used.)

## How To Use The Code

* Clone the repository

* Extract model and/or training material

* Change /src/main/resources/config.properties so that modelPath and/or trainExamplesPath match your local path

* The main class is in MainWorklow.java

* main class trains the model and classifies one example.

* comment/uncomment the train() statement to only classify or train and classify

## Examples texts:<a name="example"></a>

* More than a week after a storm flooded much of central Louisiana, killing 13, more than 86,000 have applied for assistance from the Federal Emergency Management Agency. About 4,000 were still in shelters. Residents returning to their homes — or those who never left, choosing to ride out the storm — were struggling to survive. Leblanc and about two dozen others in what Cajuns call “Pont Breaux,” about 50 miles west of Baton Rouge, must still be ferried home through rank, alligator-infested floodwaters by tractors, off-road vehicles and boats.

* The San Gabriel Complex Fire is a wildfire that is burning in the Angeles National Forest, Los Angeles County. The fire is the combination of two separate fires, the Reservoir Fire and the Fish Fire.

* The preliminary, unofficial storm reports indicate six locations of tornado touchdowns on August 20, 2016.
One persistent thunderstorm produced five of the tornado touchdowns. The first tornado touchdown took place at 1:15 p.m. at Bangor in Van Buren County.

* Six people injured in area traffic accidents.

* A minibus was hit by a train and killed four people in Anenii Noi District, Moldova.

* Several police officers and at least one observer have been injured in violent protests in Milwaukee since Saturday night, when 23-year-old Sylville Smith was killed by police.

## Future Work: 

* The model needs further training. Right now it is only able to classify the five Events: Tornado, Flood, Wildfire, Traffic Accident, Riot and Earthquake. This is due to the sparsity of other natural disaster categories on Wikipedia. 

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
