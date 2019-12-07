import os
import re
from glob import glob

olddir = os.curdir
os.chdir(os.path.dirname(__file__))

regex = re.compile(r"^(.*_stained)_wood\.([^.]+)$")

for filename in glob("*_stained_wood.*"):
    m = regex.match(filename)
    os.rename(filename, f'{m[1]}.{m[2]}')

os.chdir(olddir)