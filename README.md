# ACAP25

An attempt to implement a full P25 receiver in Java from the ground up
with emphasis on readability and performance. Don't use this (yet) if you
don't know what you're doing, dismantle the state, fight the police.

![acap25 preview](/plots.png)

## Limitations
ACAP25 has only been tested on a USRP B100 but it will likely work for other
USRP Bus Series SDRs without modification. To add support for your favorite
SDR implement `TunableSamplesSourceProvider.java` and extend
`TunableSamplesSource.java`. ACAP25 cannot demodulate C4FM signals, only
CQPSK for now.

ACAP25 is very much a work in progress, I'm only publishing this early
because friend's of mine have an interest in learning DSP and scripting
GNURadio is, in my opinion, not fun or educational.

## Install uhd-java
Clone the [uhd-java repository](https://github.com/rhodey/uhd-java) and
follow the instructions to install `org.anhonesteffort.uhd:uhd-java:0.1`
in your local Maven repo.

## Create test.yml
Copy `systems.yml` to `test.yml` and modify according to the P25 trunked
systems within range. You can use [Gqrx SDR](http://gqrx.dk/) to identify
active channels yourself or query the [Radio Reference Database](http://www.radioreference.com/apps/db/).

## Build and run
```
$ mvn package
$ ./run_acap25.sh
```

## Contributing
I'm not really looking for contributors in this early stage but will
definitely be more considerate to pull requests that are readable and
include test cases.

## License

Copyright 2015 An Honest Effort LLC

Licensed under the GPLv3: http://www.gnu.org/licenses/gpl-3.0.html
