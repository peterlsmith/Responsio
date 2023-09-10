# Responsio
A lightweight java based chatbot server and javascript client library.

Based on OpenNLP, **Reponsio** is a small, yet highly capable java based chatbot server suitable for deployment in Tomcat or other servlet container. Combined with a small javascript client library for embedding into web sites, it can easily be configured with intents, actions, and stories. With multi-language support, named entity recognition, and easy customization on both the server and client side, Responsio can handle most common chatbot scenarios.

---

## Overview
Like most chatbots, Responsio is based around an **intent** recognition system, the purpose of which is to determine what a user input means. For example, a 'greeting' intent would be determined from user input such as 'hello', 'hi there', 'howdy', 'good morning', etc. Intents may also include named 'entities' - specific things that are important to the intent, such as the location when querying what the weather forecast is, or an email address when requesting further information to be sent. Responsio uses [OpenNLP](https://opennlp.apache.org/) for intent and named entity recognition by default, but also includes support for regular expression intent matching.  

Intents are paired with appropriate actions (responses) and combined into stories. A story represents a clearly defined coversation or narrative that is supported by the chatbot. A complete set of intents, stories, and actions is called an identity. A single Responsio server instance may handle multiple chatbot identities.  

Responsio uses a number of simple json files to configure an identity. This includes the intents, stories, named entities, and utterances (output actions), as well as more fine control over the categorizers (for intent recognition), and the input processing pipeline.

---

## Terminology
Throughout this documentation, the following terminology is used.

 - **identity** a specific chatbot instance (persona), represented by a set of stories designed to address a given problem domain. Each identity within a running Responsio instance must have a unique name.

 - **story** a series of intents and corresponding actions that make up a defined conversation - for example, a story for inquiring about the status of an order will likely contain intents to determine when a customer is requesting order information and to extract order IDs, along with actions to look up the order and return the information back to the customer.

 - **intent** a specific meaning or 'intent' of the input entered by the user.

 - **action** the action to perform in response to a recognized user intent. Note that actions are defined in the context of a story, so for example, an 'affirmative' intent (e.g. 'yes', 'sure', 'ok', etc) may trigger a different action depending upon the active story. Most actions are **utterances** - actions that send output to the user, but new actions can easily be implemented perform more sophisticated tasks such as looking up data in a database or calling another API.

 - **utterance** an action that defines output to be returned to the user in response to their recognized intent.

 - **categorizer** a component of the chatbot that attempts to determine the intent of a user input. Categorizers usually take the user input and assign a probability to each configured intent indicating how likely that intent matches what the user is saying.

 - **training** the process of configuring an individual component of the chatbot system to correctly identify or recognize user input specific to the identity. In some cases, such as OpenNLP components, training may require significant processing to produce statistical models, while in other cases, such as regular expression components, it is more akin to a setting a simple configuration parameter or two. However, in both cases, the process has been abstracted into a generic 'training' step.

 - **entity** (or **named entity**) A specific value extracted from the user input that is relevant to the coversation. For example, a location, time, name, etc.  

---

## Building
Build scripts are provided for Ant, Gradle and Maven. In each case, the build scripts provide for the downloading of any necessary dependencies (e.g. data files and libraries) and will produce both a jar file and a war file by default. The jar file contains just the source code and any class specific data files. The war file contains everything in the jar file, plus the two runtime dependency libraries (OpenNLP and Gson), and a number of OpenNLP specific data models. All identity configuration is provided externally to the application.

### Ant
To build Responsio using ant, at the command prompt in the root project directory (the one with the **build.xml** file), type:

> ant

Build products will be put into the <code>dist</code> directory. Additionally, the following files will be downloaded (if they do not already exist)

<pre>
libs/jakarta.servlet-api-5.0.0.jar
libs/opennlp-tools-2.1.0.jar
libs/gson-2.10.jar
libs/hamcrest-core-1.3.jar
libs/junit-4.13.2.jar
data/models/opennlp/en-pos-maxent.bin
data/models/opennlp/en-lemmatizer.dict
data/models/opennlp/lang.bin
</pre>


### Gradle
To build Responsio using Gradle, at the command prompt in the root project directory (the one with the **build.gradle** file), type:

> gradle build

This will automatically download the necessary OpenNLP data files, compile the source code, and bundle up the jar and war files. The build products will be put into the <code>build</code> directory. Additionally, the following files will be downloaded (if they do not already exist)

<pre>
data/models/opennlp/en-pos-maxent.bin
data/models/opennlp/en-lemmatizer.dict
data/models/opennlp/lang.bin
</pre>


### Maven
To build Responsio using Maven, at the command prompt in the root project directory (the one with the **pom.xml** file), type:

> mvn package

This will automatically download the necessary OpenNLP data files, compile the source code, and bundle up the jar and war files. The build products will be put into the <code>target</code> directory. Additionally, the following files will be downloaded (if they do not already exist)

<pre>
data/models/opennlp/en-pos-maxent.bin
data/models/opennlp/en-lemmatizer.dict
data/models/opennlp/lang.bin
</pre>


---

## Testing

---

## Deploying
Responsio is designed to be deployed in a servlet container such as Tomcat using the war file build product.

Operationally, Responsio requires one initialization parameter to define a root directory that will be used for assistant configuration and logging. Generally, this is configured via the servlet container - for example, by adding the following line to <code>context.xml</code> in the <code>&lt;Context></code> element.

<pre>
  &lt;Parameter name="responsio.workdir"
    value="&lt;directory>"
    override="true"
    description="Root working directory for this application"/>
</pre>


Where <code>&lt;directory></code> is the desired disk location. Note that this directory MUST be accessible (read/write) by the user account under which the servlet container is running. This configuration value is expected to be set once only.

Alternatively, this may be configured in the application web.xml file manually prior to building/packaging by adding a <code>&lt;init-param></code> element to the <code>&lt;servlet></code> element.

<pre>
    &lt;init-param>
        &lt;param-name>responsio.workdir&lt;/param-name>
        &lt;param-value>&lt;directory>&lt;/param-value>
    &lt;/init-param>
</pre>

## Setup

The operational directory used by Responsio contains the following:

 - The main application configuration that may be changed periodically - such as logging levels, supported identities, etc. Many of these configuration parameters can be overridden by assistant specific values.

 - Identity configuration files and data.

 - Log files. Responsio writes a service level log file **responsio.log** and identity specific logs files.

The directory structure is as follows:
<pre>
    &lt;root directory>
            |
            |-- cfg
            |    |
            |    |- responsio.properties
            |
            |-- identity
            |    |
            |    |- prototype
            |    |...
            |
            |-- logs
</pre>

A baseline version of <code>responsio.properties</code> can be found in the projects <code>data</code> directory.

---

## Creating an Identity
An identity is built from a series of configuration files, each of which controls some aspect of the identity, such as an intent, utterance, action, story, etc. Once all the configuration files have been created, **training** must be performed. Training builds the NLP models and assembles all the neccessary operational configuration, along with model files. See the separate [identity configuration](docs/md/IdentityConfiguration.md) documentation for details of these configurtion files.  

Responsio comes with an example identity 'prototype' which demonstrates much of the available functionality. You can find the configuration files for this in the <code>data/identities/prototype</code> directory.


---

## Training

---

## Extending

### Custom Actions


---

## Dependencies

Responsio has a minimal set of dependencies.

 - **opennlp-tools-2.1.0.jar** Required for compilation and the runtime. This library is bound into the war file.

 - **gson-2.10.jar** Required for compilation and the runtime. This library is bound into the war file.

 - **jakarta.servler-api-5.0.0.jar** Required for compilation only. This provides the servlet API for compilation. Tomcat will provide the servlet API at runtime.

 - **junit-4.13.2.jar** Required only for compiling and running the unit tests.

 - **hamcrest-core-1.3.jar** Required only for compiling and running the unit tests.

