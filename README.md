# ACAP25

An attempt to implement a full P25 receiver in Java from the ground up
with emphasis on readability and performance. Don't use this (yet) if you
don't know what you're doing, dismantle the state, fight the police.

![acap25 preview](/plots.png)

## Limitations
ACAP25 is very much a work in progress, I'm only publishing this early
because friend's of mine have an interest in learning DSP and scripting
GNURadio is, in my opinion, not fun or educational. ACAP25 cannot demodulate
C4FM signals, only CQPSK for now.

## Build jmbe
Clone the [jmbe repository](https://github.com/DSheirer/jmbe) and follow
the instructions to build `jmbe-x.x.x.jar`. Copy the resulting jar into
`import/`.

## Build and install
```
$ mvn install package
```

## Chose a sample source
ACAP25 uses the Java SPI pattern to allow for modular software defined
radio support. To add support for your SDR simply extend `org.anhonesteffort.p25.sample.TunableSamplesSource`
and implement `org.anhonesteffort.p25.sample.TunableSamplesSourceProvider`,
then add these files to your java classpath.

Currently the following ACAP25 drivers are available:
  +  Ettus USRP SDRs - [acap25-usrp](https://github.com/rhodey/acap25-usrp)

## Create test.yml
Copy `systems.yml` to `test.yml` and modify according to the P25 trunked
systems within range. You can use [Gqrx SDR](http://gqrx.dk/) to identify
active channels yourself or query the [Radio Reference Database](http://www.radioreference.com/apps/db/).

## Run
```
$ ./run_acap25.sh
```

## Contributing
I'm not really looking for contributors in this early stage but will
definitely be more considerate to pull requests that are readable and
include test cases.

## License

Copyright 2015 An Honest Effort LLC

Licensed under the GPLv3: http://www.gnu.org/licenses/gpl-3.0.html
