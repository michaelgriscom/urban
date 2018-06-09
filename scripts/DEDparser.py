import sys
import os

# I don't like using windows cmd prompt so I hard coded these
docFolder = "C:\\Users\\Michael\\CSEprojects\\560-project\\src\\linker"
outfilepath = "C:\\Users\\Michael\\DED.txt"

def parsefile(filename):
    i = 0
    fileDED = {}
    
    comment = ""
    comment_open = False
        
    # parse each file ('with' handles opening/closing streams but is only 2.5+)
    with open(os.path.join(docFolder, filename), 'r') as f:
        for line in f:
            i += 1
            
            if comment_open:
                if "]" in line:
                    comment_open = False
                comment += line.strip()[2:]
                
            elif "// [" in line or "//[" in line:
                comment_open = True
                comment = line.strip()[2:]
                if "]" in line:
                    comment_open = False
                    fileDED[i] = comment.strip()
                    comment = ""

        return fileDED

with open(outfilepath, 'w') as outFile:

    # only parse java files
    extension = ".java"
    javaFiles = [file for file in os.listdir(docFolder) if file.lower().endswith(extension)]

    for filename  in javaFiles:
        print("Parsing " + filename)
        
        fileDED = parsefile(filename)

        for i,rawLine in fileDED.items():
            entry = rawLine.lstrip("[").rstrip("]").split(None, 3)
            
            if len(entry) < 4:
                print("Line ignored: " + filename + " line found: " + rawLine)
                continue
               
            vname = entry[0]
            vtype = entry[1]
            vrange= entry[2][0].capitalize() + entry[2][1:] # capitalize first letter

            if (vrange != "Global") and (vrange != "Local"):
                vrange = "Local"
                vcomment = vrange + entry[3]
                print("Assuming local scope: " + filename + " line found: " + rawLine)
        
            vcomment = entry[3][0].capitalize() + entry[3][1:]
            
            outFile.write(filename + "\t" + vname + "\t"
                          + vtype + "\t" + vrange + "\t"
                          + vcomment + "\n")


