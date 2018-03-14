#!/bin/bash

inputs=($(ls | egrep '^input.$'))
outputs=($(ls *.xml))
len=${#inputs[@]}

echo "inputs len: $len"
for ((i=0; i<${len};++i)); do
    echo " $i. Input file: ${inputs[$i]}"
    php5.6 ../parse.php < ${inputs[$i]} > "${inputs[$i]}.out"
    echo "diff ${outputs[$i]} ${inputs[$i]}.out: "
    diff "${outputs[$i]}" "${inputs[$i]}.out"
    printf "\n\n Press enter for next test:"
    read a
done