JAVA_SRCS:=$(wildcard tasks/src/*.java)
JAVA_CLASSES=$(subst /src/,/build/,$(JAVA_SRCS:.java=.class))
JFLAGS=-cp octobot.jar -d tasks/build
JC=javac

.SUFFIXES: .java .class

.java.class:
 $(JC) $(JFLAGS) $*.java

default: build

build: $(JAVA_CLASSES)

clean: