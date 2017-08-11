# KeeN
KeeN is a **K**nowledge **E**ngineering **EN**vironment for [Timeline-based Planning](https://ugilio.github.io/keen/intro) (DDL.3 language). It is based on the [Eclipse](https://www.eclipse.org) platform and is meant to ease the developing of planning domains and problems, both using the traditional features that are expected from a modern Integrated Development Environment and through the graphical modeling of domains by the means of diagrams.

KeeN supports the so-called *Round-trip Engineering*, permitting the developers to employ the diagram environment also to *edit* existing code, and in general enabling them to seamlessly switch between the code-based and diagram-based views without loss of information.

The environment can make use of existing planners to help the developers test their solution; moreover, it supports Domain Validation and Plan Verification through the integration of existing code and tools that were realized as a consequence of recent research in the area.

KeeN was developed by Giulio Bernardi at the [Planning and Scheduling Technology Laboratory](http://istc.cnr.it/group/pst). It should be considered *alpha* quality software. It is Open Source software released under the [Eclipse Public License](https://www.eclipse.org/legal/epl-v10.html).

### Getting Started

To install: add `https://pst.istc.cnr.it/keen/updatesite` as update site to your Eclipse Installation; read the [user guide](https://ugilio.github.io/keen/userguide) for more information about installation and for some tutorials.

### Building from Source

Clone the repository:
```
git clone https://github.com/ugilio/keen.git
cd keen
```

and then build (requires [Maven](https://maven.apache.org/) and Java 8):
```
cd it.cnr.istc.keen.releng
mvn package
```

### More Information

[Website](https://ugilio.github.io/keen)

[User Guide](https://ugilio.github.io/keen/userguide)

[Introduction to Timeline-based Planning](https://ugilio.github.io/keen/intro)

