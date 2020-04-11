import os, sys
import re
import subprocess
from subprocess import PIPE
from glob import glob

DO_MODELS       = 0
DO_LANG         = 0
DO_BLOCKSTATES  = 0
DO_TEXTURES     = 0
DO_RECIPES      = 1
DO_TAGS         = 0

REPLACE_MODELS      = 1
REPLACE_LANG        = 1
REPLACE_BLOCKSTATES = 1
REPLACE_TEXTURES    = 0
REPLACE_RECIPES     = 1

    
olddir = os.curdir
os.chdir(os.path.dirname(__file__))

OUT = R'..\src\main\resources\assets\storagedrawersunlimited'

# moddirs = set(glob(Rf'{OUT}\textures\block\*')) - {R'{OUT}\textures\block\templates.png'}
moddirs = {Rf'{OUT}\textures\block\{name}' for name in ('biomesoplenty', 'glacidus', 'traverse', 'natura', 'goodnightsleep', 'blue_skies', 'quark')}

material_name_regex = re.compile(r"^base_([_\w]+)\.png$")

def extract_material_name(path: str) -> str:
    path = os.path.basename(path)
    m = material_name_regex.match(path)
    return m[1]

for modpath in moddirs:
    modid = os.path.basename(modpath)
    if modid == 'natura':
        materials = ['bloodwood']
    else:
        materials = map(extract_material_name, glob(Rf'{modpath}\base\base_*.png'))

    print(f'Processing materials for mod {modid}:')

    for material in materials:
        print(f'\t{material}')
        args = [
            'java', 
            '-jar', R'ResourceCreator\target\ResourceCreator-1.0-jar-with-dependencies.jar', 
            '-out', OUT,
            '-templates', R'ResourceCreator\templates',
            '-modid', modid,
            '-base', Rf'{modpath}\base\base_{material}.png',
            '-trim', Rf'{modpath}\base\trim_{material}.png',
            '-face', Rf'{modpath}\base\face_{material}.png',
            '-materials', Rf'{modpath}\base\materials_{material}.png',
            '-automaterials',
            '-i', # If trim image or face image files don't exist, act like they weren't even specified
            '-iquiet', # Don't print warning messages when the trim or face image files don't exist
            '-backup', 'lang',
        ]
        
        if DO_LANG:
            if not REPLACE_LANG:
                # Don't replace language entries if they exist
                args.append('-noreplacelang')
        else:
            args.append('-nolang')
        
        if DO_BLOCKSTATES:
            if not REPLACE_BLOCKSTATES:
                # Don't replace blockstate files if they exist
                args.append('-noreplaceblockstates')
        else:
            args.append('-noblockstates')

        if DO_MODELS:
            if not REPLACE_MODELS:
                # Don't replace model files if they exist
                args.append('-noreplacemodels')
        else:
            args.append('-nomodels')

        if DO_TEXTURES:
            if not REPLACE_TEXTURES:
                # Don't replace texture files if they exist
                args.append('-noreplaceimages')
        else:
            args.append('-noimages')

        if DO_RECIPES:
            if not REPLACE_RECIPES:
                # Don't replace recipe files if they exist
                args.append('-noreplacerecipes')
            args += ['-materials', Rf'{modpath}\base\materials_{material}.json']
        else:
            args.append('-norecipes')

        if DO_TAGS:
            pass
        else:
            args.append('-notags')

        subprocess.run(args, stdout=sys.stdout, stderr=sys.stderr)


os.chdir(olddir)