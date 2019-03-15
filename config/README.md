# MIRO360 Configuration
MIRO360 is a tool for subjective assessment of 360 degree video compatible
with the (draft) Recommendation ITU-T P.360-VR.


This README describes the format of the configuration file [`miro360.json`](miro360.json).

The file has this format

```
{
  "scales": [ {...}, {...} ],
  "questionnaires": [ {...}, {...} ],
  "sequences": [ {...}, {...} ],
  <global_options>
}
```

The only mandatory section is `"sequences"`, which describes the sequences used in the test.
 
## Scales
This section allow user to define 5-category categorical scales.

The element `"scales"` is a list of (zero or more) objects with the following format:

```
{
  "name" : "acr",
  "scores": ["bad", "poor", "fair", "good", "excellent"]
}
```

- `name` is the scale name
- `scores` is a list of 5 strings representing categories from 1 to 5.

### Built-in scales

- acr
- dcr
- vertigo
- dizzy
- likert


## Questionnaires

Post-experience questionnaire. Each element in list `"questionnaires"` has the format:

```
{
  "name": "acr",
  "items": [
    {
      "scale": "acr",
      "text": "Please rate the quality of the sequence",
      "tag": "ACR"
    }
  ]
}
```

- `name` is the name of the questionnaire, as it will be used in the configuration of the post-sequence questionnaires.
- `items` is a list of items (i.e. questions). Traditional quality questionnaires (ACR, DCR) are single-item, but presence questionnaires are normally multi-item.

## Sequences

List of sequences to be played. 

The format is as follows:

```
{
	"uri": "Movies/example.mp4",
	
	"start": 10.4,
	"duration": 30,
	"orientation": 90,
	"src_id": "example",
	
	"in_seq_method": "sscqe",
	<in-seq configuration fields>
	
	"post_seq_questions": [ ... ]
}
```

### URI

`"uri"` is the only mandatory parameter of each sequence. It must be a path to a file under the [primary shared/external storage directory](https://developer.android.com/reference/android/os/Environment.html#getExternalStorageDirectory()).

### Sequence parameters
The following parameters are optional:

- `start` (default = 0) Start time of the sequence, in seconds. 
- `duration` (default = 0) Duration of the sequence to be played. Set to 0 to play the whole file described by `uri`.
- `orientation` (default = 0). Orientation of the viewer with respect to the sequence at the beginning of the playout.
- `source_id`. An unique identifier of the source content ("SRC") of this sequence. Used only for the randomization process (see below).


### In-sequence evaluation parameters
In-sequence evaluation method (`in_seq_mehod`) can be:

- `"sscqe"` Single-Stimulus Continuous Quality Evaluation. The format is:

```
{
  "in_seq_method": "sscqe",
  "in_seq_scale": "acr"
}
```
- `"ssdqe"` Single Stimulus Discrete Time Evaluation. The format is:

```
{
  ...
  "in_seq_method": "sscqe",
  "in_seq_scale": "acr",
  "ssdqe_start": 40,
  "ssdqe_period": 30,
  "ssdqe_duration": 10,
  "ssdqe_total_number": 4
  ...
}
```
- `""` (default value): no in-sequence evaluation.


Both SSCQE and SSDQE allow to select the categorical scale they use. Any built-in or user-defined **Scale** can be selected, just by naming the **Scale** name (see above).

SSDQE requires additional parameters:

- `ssdqe_start` (seconds): instant of the first SSDQE question (since the beginning of the sequence).
- `ssdqe_period` (seconds): periodicity of the questions, starting at `ssdqe_start`.
- `ssdqe_duration` (seconds): maximum time that the user has to score each question; after that time, if the user has not scored, the SSCQE question disappears from the screen.
- `ssdqe_total_number`: total number of SSDQE questions in the sequence.


### Post-sequence evaluation
Configuration of post-sequence evaluation is as simple as: 

```
  "post_seq_questions": [ "acr", "mec", ... ]
```

- `post_seq_questions` is a list of **Questionnaires** (see above) that are going to be asked after the sequence finishes playing.
- Any built-in or user-defined **Questionnaire** can be used.
- **Questionnaires** are applied in the same order they appear in the `post_seq_questions` list.
- However, items within each (multi-item) **Questionnaire**  are randomized each time.


## Global Options

There are some global options in the configuration file `miro360.json` that apply to the whole test session.

They are:

```
{
...
	"randomize": true,
	"max_random_tries": 1000,
	"pause_after_sequence": false
}
```

- `randomize` (default = false) enables the randomization of the sequences in the test session. Randomization is done in a way that, if `source_id` has been defined for each sequence, no sequences with the same `source_id` can appear consecutively in the session. This conditional randomization has been applied by simply running the randomization many times until the condition is fulfilled. To avoid infinite loops, there is a limit of `max_random_tries` iterations (default = 1000).
- `pause_after sequence` (default = false) forces that after finishing all the post-sequence questionnaires of each sequence, a "click to continue" screen appears and the test is paused until the user clicks the controller.
 

## License

MIT License

Copyright (c) 2019 Nokia

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
