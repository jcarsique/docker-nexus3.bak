#!/usr/bin/env python3

import json
import sys

SCRIPT_PATH="/opt/sonatype/nexus/scripts/"

with open(SCRIPT_PATH + sys.argv[1] + ".groovy", "r") as inputfile:
    filedata = inputfile.read()
    jsondata = {}
    jsondata['name'] = sys.argv[1]
    jsondata['type'] = 'groovy'
    jsondata['content'] = filedata

    with open(SCRIPT_PATH + sys.argv[1] + "-body.json", "w") as outputfile:
        outputfile.write(json.dumps(jsondata))
