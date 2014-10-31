#!/bin/bash
java -cp `ls ./lib/*.jar|tr '\n' ':'` es.uvigo.ei.aibench.Launcher plugins_bin
