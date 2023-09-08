# Creating an Identity
## Overview

Creating an identity - a unique chatbot persona in Responsio - requires a number of configuration files for configuring intents, utterances (responses), stories, and a whole host of other data. All of these configuration files are placed into a single directory structure as follows:
<pre>
    data/identities/&lt;identity>
            |
            |-- intents
            |    |- greet.json
            |    |- affirm.json
            |    |- get_weather.json
            |    |- ...
            |
            |-- stories
            |    |- weather.json
            |    |- ...
            |
            |-- extensions
            |    |- ActionWeather.java
            |    |- ...
            |
            |-- categorizers.json
            |-- misc.json
            |-- parameters.json
            |-- pipeline.json
            |-- utterances.json
            |-- welcome.json
            |-- welcome-back.json
</pre>
Where <code>&lt;identity></code> is the name of the identity being configured.  

All of these configuration files will be processed by the training tool in order to produce the necessary OpenNLP model files and the aggregated identify configuration file that will be used by the Responsio web service. Each of these configuration files/directories is covered in more detail below.

The process of generating a response to a user input has a number of discreet steps as follows:

+ The user input is pre-processed - case conversion, removing stop words, tokenization, stemming, etc - base on [the pipeline configuration](#processing-pipeline). The goal of this step is normalize the user input into a more consistent text for categorization. For example, this might involve expanding certain contractions such as <code>I'm</code> to <code>I am</code>. This can help reduce the amount of training required.

+ The pre-processed input is categorized. This attempts to determine the meaning of the user input. The accuracy of this step depends largely on the quality of the training data configured in the [intent configuration files](#intents) used to train the categorizers.

+ If the categorized intent has any configured name entities, named entity extraction will be performed.

+ The resulting intent and any named entities, along with previous user input, will be matched against the [stories](#stories) in an attempt to determine the current point in a 'conversation'.

+ Actions associated with the matching story point will be executed, generating the response that will be returned to the user.

---  


## Categorizers
The <code>categorizers.json</code> configuration file defines the intent categorizers that will be used in the chatbot. Responsio supports two different categorizers out of the box - an OpenNLP based categorizer and a regex categorizer. Most chatbots will just need the NLP categorizer, but there can be instances where the regex categorizer might be needed - for example, when a very specific, limited set of commands or key words is expected. A typical categorizer configuration file will look as follows:
<pre>
{
    "default": {
        "type": "com.paradoxwebsolutions.assistant.categorizers.CategorizerDefault",
        "defaultConfidenceThreshold": 0.75,
        "input": "tokens",
        "trainingInput": "tokens"
    }
}
</pre>
This specifies a single categorizer that will be named '<code>default</code>' (this name will be used in other configuration files, and can be set to any reasonable value). The supported parameters are:

- <code>type</code> Specifies the class name of the categorizer - in this case the class name is that of the default Responsio categorizer (which uses OpenNLP).

- <code>defaultConfidenceThreshold</code> Defines the probability threshold for which intents will not be considered a match. Categorizers assign a probability value from 0 to 1 to each intent based on its liklihood of being a match to the user input. This parameter tells it to discard any intent with a probability less than the configured value. Ultimately, the intent with the highest probability will be selected as 'the truth', but there may be cases where all intent probabilities fall below this threshold and the chatbot cannot determine a likely intent. In this case, you can configure a default intent in the [Miscellaneous Configuration](#miscellaneous-configuration) which can be used to handle the "I'm not sure what you are asking" scenario.

- <code>input</code> Gives the name of the processing pipeline output to be used as the input for intent recognition. Refer to [processing pipeline configuration](#processing-pipeline) for more details.

- <code>trainingInput</code> Gives the name of the processing pipeline output to be used as the input for intent recognition trainging. Refer to [processing pipeline configuration](#processing-pipeline) for more details.  

If no categorizer configuration file is provided, a default one matching the configuration example above will be used automatically.  

An identity may use more than one categorizer - for example, an identity that requires both the OpenNLP categorizer and the Regex categorizer might looks as follows:
<pre>
{
    "default": {
        "type": "com.paradoxwebsolutions.assistant.categorizers.CategorizerDefault",
        "defaultConfidenceThreshold": 0.75,
        "input": "tokens",
        "trainingInput": "tokens"
    },
    "regex": {
        "type": "com.paradoxwebsolutions.assistant.categorizers.CategorizerRegex",
        "input": "sentence",
        "trainingInput": "document"
    }
}
</pre>
The configuration of the regex categorizer is similar to the OpenNLP categorizer. The main differences are that a regex categorizers only produces two probabilties - 0 or 1, and thus does not need a confidence threshold, and it generally uses different pipeline inputs.

---  


## Intents
Intents are a key part of an identity configuration as they form the basis of stories (conversations). An intent is a general 'meaning' applied to user input. For example, a '<code>greet</code>' intent may represent a user saying 'hello'. However, there are many different ways in which the user can say this. The purpose of an intent file is primarily to provide training data that allows the chatbot to determine whether or not a given user input represents a given intent.  

A simple intent configuration file used to identify a greeting might be as follows:

<pre>
{
    "intent" : "greet",
    "train": [{
            "type": "categorizer",
            "name": "default",
            "data": "default",
            "pipeline": "train"
        }
    ],
    "data": {
        "default" : {
            "en": [
                "Howdy",
                "Hello",
                "Hello, how are you",
                "How do you do",
                "gday mate",
                "Hi there",
                "Hi",
                "Good morning",
                "Good afternoon",
                "Sup!"
            ],
            "fr": [
                "salut",
                "bonjour",
                "bonsoir",
                "coucou",
                "Allô",
                "bienvenue",
                "enchanté"
            ]
        }
    }
}
</pre>
The parameters are as follows:

- **intent** gives the name of this intent. It is not required, but it is recommended that the name of the configuration file should be the name of the intent (in lower case) with <code>.json</code> appened.

- **train** controls aspects of the training of any categorizers and/or named entity recognition for this intent.  
    * **type** the type of component being trained - either '**categorizer**' or '**ner**'.
    * **name** the name of the component being trained. In the case of a categorizer, this will be the name defined in the [categorizers configuration file](#categorizers). In the case of name entity recognition (NER), it will be the name of the NER as defined in the intent configuration file (an example of this is given below).
    * **data** the name of the dataset as found in the **data** parameter described below
    * **pipeline** the name of the data processing pipeline to be used for preparing the data for training  

- **data** training datasets. This may specifiy multiple named datasets, with each dataset consisting of a number of language specific data values. The example above specifies a single dataset named '<code>default</code>' with english and french language sets.  

The intent configuration may also contain named entity recognition (NER) configuration which can be used to extract information (named entities) from the user input. For example, a '<code>get_weather</code>' intent may provide named entity configuration for extracting location and temporal information from the user input when asking for a weather forecast.  

<pre>
{
    "intent" : "get_weather",
    "ners" : {
        "ner-1": {
            "type": "com.paradoxwebsolutions.assistant.ners.NERDefault",
            "input": "tokens"
        }
    },
    "train": [{
            "type": "categorizer",
            "name": "default",
            "data": "default",
            "pipeline": "train"
        },{
            "type": "ner",
            "name": "ner-1",
            "data": "default",
            "pipeline": "train",
            "parameters": {
                "input": "ner",
                "cutoff": 1,
                "iterations": 70
            }
        }
    ],
    "entities": {
        "temporal": {
          "local": true
        }
    },
    "data": {
        "default" : {
            "en": [
                "what is the weather?",
                "what is the weather forecast?",
                "What is the weather like in {location:Boise, Idaho}?",
                "Is it going to rain in {location:New York} {temporal:today}?",
                "Tell me the forecast for {location:Houston, TX} {temporal:tomorrow}",
                "Tell me the forecast for {temporal:Wednesday} in {location:Columbus}",
                "Tell me the forecast for {location:Alamora}",
                "Is it going to be sunny {temporal:tomorrow}?",
                "Is it going to rain {temporal:tomorrow}?",
                "When is the rain arriving {temporal:today} in {location:LA}?",
                "Can you tell me the weather for {location:Yorktown}",
                "Can you tell me the forecast for {location:Middlesburg}",
                "Can you tell me the weather for {location:Yorktown} for {temporal:next week}",
                "Can you tell me the weather for {location:New Orleans} this {temporal:week}",
                "Is it going to rain in the {location:White Mountains} on {temporal:Thursday}?",
                "How is the weather in {location:Jericho} {temporal:today}?",
                "Is it going to be nice and cool in {location:Fresco} {temporal:tomorrow}?",
                "Is it warm in {location:Death Valley} {temporal:today}?",
                "How is the weather in {location:Toronto} {temporal:currently}?",
                "What's it going to be like in {location:Springfield, MD} {temporal:next week}?",
                "Can you give me the weather for {location:LA} for {temporal:tomorrow}?",
                "What's the forecast for {location:Philadelphia}?",
                "What's the forecast for {location:Boston} on {temporal:Wednesday}?",
                "What is the weather in {location:Richmond} this {temporal:weekend}?",
                "Are there any thunderstorms forecast for {temporal:tonight} in {location:Miami}?",
                "Give me the forecast for {location:Baja} on {temporal:Monday}?",
                "What's the weather in {location:Tuscon} on {temporal:Tuesday}?",
                "I want the forecast for {location:Charlotte} on {temporal:Wednesday}?",
                "What's the weather going to be like in {location:New York} on {temporal:Thursday}?",
                "What's the weather in {location:Memphis} on {temporal:Friday}?",
                "Forecast for {location:Seattle} on {temporal:Saturday}?",
                "What's the weather in {location:Portland} on {temporal:Sunday}?",
                "What's the weather going to be like in {location:Houston} on {temporal:Tuesday}?",
                "What's the forecast for {location:Tampa} on {temporal:Monday}?",
                "What's the forecast for {location:Tampa} {temporal:next weekend}?",
                "What's the weather going to be like on {temporal:Saturday} in {location:Kingston}?",
                "Weather in {location:Denver} on {temporal:Wednesday}?",
                "What's the weather forecast for {location:Phoenix} on {temporal:Thursday}?",
                "What is the forecast for {location:Dublin} this coming {temporal:week}?",
                "I need the weather forecast for {location:Dallas} {temporal:next week}?",
                "Give me the forecast for {location:Albuquerque} this {temporal:week}",
                "Give me the forecast for {location:washington DC}",
                "Give me the forecast for {location:Sacramento} on {temporal:Tuesday}",
                "Weather for {location:Witicha}?"
            ]
        }
    }
}
</pre>
This example includes an OpenNLP based named entity recognizer and the training configuration it requires. New/modified parameters are as follows:

- **ners** The NER configuration (one or more). This example defines one OepnNLP based NER (com.paradoxwebsolutions.assistant.ners.NERDefault) named '**ner-1**' that will use the **tokens** output from the processing pipeline as its input data for the purpose of NER processing. Any reasonable name can be used for the NER.

- **entities** Any special named entity configuration. By default, named entity values are extracted and stored in session information (called <code>slots</code>). This configuration entry can be used to indicate that a given named entity should be considered local (not stored in a slot). It is still available for intent matching however.

- **train** As before, we include training configuration for the default categorizer, but we now also include training configuration for the NER as follows:
    * **type** the type of component being trained - '**ner**'.
    * **name** the name of the component being trained. This should match the name of the NER as configured in the **ners** configuration block described above.
    * **data** the name of the dataset as found in the **data** configuration. In this case it is using the same training data as the categorizer
    * **pipeline** the name of the data processing pipeline to be used for preparing the data for training
    * **parameters** additional training parameters. The values given here are all default values that do not need to be specified unless you want to use something different.
        + **input** the processing pipeline output value to be used as training data input
        + **cutoff** OpenNLP training configuration parameter value for <code>TrainingParameters.CUTOFF_PARAM</code>
        + **iterations** OpenNLP training configuration parameter value for <code>TrainingParameters.ITERATIONS_PARAM</code>
- **data** similar to before, but this time the training data is annotated/tagged with <code>location</code> and <code>temporal</code> markers. These markers are needed by the NER for training, but are NOT wanted by the categorizer. We could achieve this by providing two different data sets - one for training the categorizer and the other for training the NER, but we take a simpler approach of using a special **PreprocessorNERFilter** preprocessor in the [processing pipeline](#processing-pipeline) to remove the tags.

If we want to expand the possible location values to include zip codes, we could update the **ners** configuration block to include a regex NER as follows:

<pre>
"ners" : {
    "ner-1": {
        "type": "com.paradoxwebsolutions.assistant.ners.NERDefault",
        "input": "tokens"
    },
    "ner-2": {
        "type": "com.paradoxwebsolutions.assistant.ners.NERRegex",
        "input": "document",
        "parameters": {
            "\\d{5}": "zipcode"
        }
    }
}
</pre>

This will extract any five digit value in the input and store it in the '<code>zipcode</code>' named entity. A regex NER does not require any training.

---  

## Utterances
An utterance is simply a text message that will be returned to the user in response to a given action. Utterances are defined in the <code>utterances.json</code> configuration file, e.g.  

<pre>
{
    "welcome" : {
        "en": "Hello, and welcome to the weather chatbot.",
        "fr": "Bonjour et bienvenue sur le chatbot météo."
    },
    "welcome-back" : {
        "en": "Welcome back!",
        "fr": "Content de te revoir!"
    },

    ...

    "oos" : {
        "en": "I'm sorry, but I did not understand that. Could you please rephrase it?",
        "fr": "Je suis désolé, mais je n'ai pas compris. Pourriez-vous s'il vous plaît reformuler?"
    }
}
</pre>

All utterances are named, and each named utterance may have multiple language versions of its text. The specific language that will be output can be explicitly configured, or may be detected automatically using the Language preprocesser in the processing pipeline. Note that if you are planning to support multiple languages, you will need to do so across ALL utterances, stories, etc. 

---  

## Stories
A story represents a conversation with the user, and can be thought of as a <code>path</code> - a series of steps, each of which specifies some criteria to match against the user input (usually an intent name) and an action to be executed. Some stories may be very simple - for example, responding to a greeting by the user, while others may be more complex, involving many steps, some of which may be optional or repeated.  
Each story is configured in its own json file. e.g. for a 'greet' story, the <code>greet.json</code> configuration file may looks as follows:  

<pre>
{
    "path": [{
        "type": "com.paradoxwebsolutions.assistant.steps.StepDefault",
        "intentMatcher": {
            "type": "com.paradoxwebsolutions.assistant.intentMatchers.IntentMatcherDefault",
            "intent": "greet"
        },
        "action": {
            "type": "com.paradoxwebsolutions.assistant.actions.ActionUtter",
            "utterance": "greet"
        }
    }]
}
</pre>

This example contains a simple path with one step. There are different types of steps, and the configuration for each step type is slightly different, but all contain the <code>type</code> attribute. This gives the class name of the step type to use - in this example, the default step type. Steps and their configurable attributes are explained in more detail below. This example also specifies an action to be executed when the step is matched against user input - in this case just a simple utterance (text output).  

Supported step types are: 

### StepDefault
This is the default step use to match againt a specific intent (and associated conditions), along with an action to execute when the step is matched successfully. It has the following attributes:  
 - **intentMatcher** The specific type of 'matcher' to use for matching against an intent. The example above uses a default matcher, designed only to check the name of the intent. Other intent matchers can be used to match not only the intent name, but entity and/or slot values. Intent matchers are described in more detail below.
 - **Action** The action to be executed when this step is matched. actions are described in more detail below.

### StepOptional
Specifies a step that is option - it may occur zero or once. It has the following attributes: 
 - **step** The optional step. This is a recursively defined step - usually an instance of <code>StepDefault</code>

### StepOr
Specifies a list of steps, only one of which must match. It has the following attributes: 
 - **steps[]** A list of recursively defined steps, one of which should match. Theese are usually instances of <code>StepDefault</code>.

### StepRepeat
Specifes a step that may be repeat 0 to n times. It has the following attributes: 
 - **step** The step to be repeated. This is a recursively defined step - usually an instance of <code>StepDefault</code>
 - **minMatch** The minimum number of times the step can be repeated to be a valid match
 - **maxMatch** The maximum number of times the step can be repeated to be a valid match

### Path
This can be thought of as a compound step, or sub-story, and is used to specific a sequence of steps, all of which must match. The type attribute for this is <code>com.paradoxwebsolutions.assistant.steps.StepDefault</code>. It has the following attributes: 
 - **path[]** A list of recursively defined steps, one of which should match. These are usually instances of <code>StepDefault</code>.


Supported intent matchers are:
### IntentMatcherDefault
This is the most basic intent matcher and simply checks the name of the intent determined from the user input. It has the following attributes:
 - **intent** The name of the intent to match against

### IntentMatcherExpression
A more complex type of intent matcher that allows fairly arbitrary expressions to match against the intent - including the intent name, named entity values, and slot values.It has the following attributes:
 - **expression** The expression to use for evaluation. At the most basic level, it can behave just like the default matcher using the intent name, e.g. the expression <code>greet</code> will match the **greet** intent. An expression such as <code>get_weather and location</code> can be used to match the **get_weather** intent that has a non-empty **location** attribute. <code>get_weather and location and slot("temporal")</code> can be used to match the **get_weather** intent that has a non-empty **location** attribute AND non-empty slot named 'temporal'.

### IntentMatcherOr
This is a compound intent matcher that requires one of a list of intent matchers to match. It has the following attributes:
 - **intentMatchers[]**  A list of recursively defined intent matchers (usually deafult or expression intent matchers)

---  

## Processing Pipeline

The processing pipeline configuration file <code>pipeline.json</code> defines what preprocessing steps should be performed on input data prior to use. A processing pipeline is made up of a series of processing steps, executed in turn. Typical processing steps include case conversion, abbreviation expansion, tokenization, etc. A processing step will specify a named input value to process, and the name of the resulting output value. It may also specific other configuration values depending upon the processing step type. At the start of pipeline processing, the input value <code>document</code> will contain the raw input value to be processed - i.e. the user input or raw training data string. A pipeline may produce multiple named output values.

Given there are different situations for which a processing pipeline is required - for example, processing user input prior to intent recognition, or processing training data prior to intent training, a processing step may also define the name of the pipeline to which it belongs. This allows use to configure different processing pipelines for the different use cases. By default, a processing step will apply to all pipelines. The only system named processing pipeline is '<code>chat</code>', which is used for processing raw user input for entity recognition. Intents may configure their own named pipelines for NER or categorizer training.  

A typical processing pipeline may look as follows:

<pre>
[
    {
        "comment": "STEP 1: Detect which language is being used",
        "type": "com.paradoxwebsolutions.assistant.preprocessors.PreprocessorLanguage",
        "input": "document",
        "output": "language",
        "pipelines": ["chat"]
    },
    {
        "comment": "STEP 2: Convert the input to lower case",
        "type": "com.paradoxwebsolutions.assistant.preprocessors.PreprocessorLowercase",
        "input": "document",
        "output": "sentence"
    },
    {
        "comment": "STEP 3: Expand any contractions",
        "type": "com.paradoxwebsolutions.assistant.preprocessors.PreprocessorExpander",
        "input": "sentence",
        "output": "sentence"
    },
    {
        "comment": "STEP 4: Output specifically for NER training",
        "type": "com.paradoxwebsolutions.assistant.preprocessors.PreprocessorCopy",
        "input": "sentence",
        "output": "ner",
        "pipelines": ["train"]
    },
    {
        "comment": "STEP 5: Remove NER tags from input",
        "type": "com.paradoxwebsolutions.assistant.preprocessors.PreprocessorNERFilter",
        "input": "sentence",
        "output": "sentence",
        "pipelines": ["train"]
    },
    {
        "comment": "STEP 6: Tokenize",
        "type": "com.paradoxwebsolutions.assistant.preprocessors.PreprocessorTokenizer",
        "delimiters": "[\\s,.!?]+",
        "input": "sentence",
        "output": "tokens"
    },
    {
        "comment": "STEP 7: Copy output for generic training",
        "type": "com.paradoxwebsolutions.assistant.preprocessors.PreprocessorCopy",
        "input": "tokens",
        "output": "train",
        "pipelines": ["train"]
    }
</pre>

This defines two main processing pipelines, '<code>chat</code>' (the system pipeline), and '<code>train</code>' (which will be used for categorizer training).  
  
When the system receives user input, it will request the '<code>chat</code>' processing pipeline that will consist of steps 1, 2, 3 and 6, which will perform language detection, lower case conversion, abbreviation expansion, and tokenization. The main outputs of this pipeline is '<code>tokens</code>' (the output from the tokenization step), which will consist of an array of words values that can be used for intent recognition. By default, the intent categorizers will use the '<code>tokens</code>' output, but they can be configured otherwise. A secondary (generally unused) output is '<code>sentence</code>', containing the lower cased, expanded input as a single string. In flowchat form, the '<code>chat</code>' processing pipeline looks like this:
  
```mermaid
    flowchart TB;
        subgraph <b>chat processing pipeline</b>
            document -- "language detect" --> language;
            document -- "lower case" --> s1[sentence];
            s1[sentence] -- expansions --> s2[sentence];
            s2[sentence] -- tokenize --> tokens
        end
```

The '<code>train</code>' processing pipeline will consist of steps 2, 3, 4, 5, 6, and 7. This performs lower case conversion, abbreviation expansion, NER filtering (removing any NER tags), and tokenization. This produces a number of output values, but the primary (useful) ones are:  
- **ner**  a value used for named entity recognition training. This will include NER tags {<name>:...}.
- **train**  the tokenized training data with NER tags removed.

In flowchat form, the '<code>train</code>' processing pipeline looks like this:
  
```mermaid
    flowchart TB;
        subgraph <b>train processing pipeline</b>
            document -- "lower case" --> s1[sentence];
            s1[sentence] -- expansions --> s2[sentence];
            s2[sentence] -- copy --> ner;
            s2[sentence] -- NER filter --> s3[sentence];
            s3[sentence] -- tokenize --> tokens
            tokens -- copy --> train
        end

```  


Other processors include:
- **PreprocessorFormatter** - can be used to combine and format multiple input values
- **PreprocessorLemmatizer** - performs word lemmatization
- **PreprocessorStemmer** - performs word stemming
- **PreprocessorPOS** - performs parts of speech tagging
- **PreprocessorStopwords** - removes irrelevant or unneccessary words

---  

## Miscellaneous Configuration
The miscellaneous configuration file <code>misc.json</code> is an optional configuration file used to specify a few configuration parameters that do not fit anywhere else. It looks like this:

<pre>
{
    "default-intent": "oos",
    "languages": ["en"],
    "welcome": {
        "type": "com.paradoxwebsolutions.assistant.actions.ActionUtter",
        "utterance": "welcome"
    },
    "welcome-back": {
        "type": "com.paradoxwebsolutions.assistant.actions.ActionUtter",
        "utterance": "welcome-back"
    }
}
</pre>

Supported attributes are:

- **default-intent** The intent to use when the user input cannot be categorized. 
- **languages**      The langauges supported for this identity
- **welcome**        The action to execute when a new user connects for the first time
- **welcome-back**   The action to execute when a user returns after an absence

The example <code>misc.json</code> file above shows the default settings, so if this suits your needs, you do not need to provide this configuration file.

