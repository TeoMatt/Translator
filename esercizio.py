import nltk
from nltk import load_parser
cp = load_parser('ex3_per.fcfg')
#query = 'the great jedi of the great mind uses a great one for the great knowledge'
query = 'a jedi makes the children great'
trees = list(cp.parse(query.split()))
answer = trees[0].label()['SEM']
#print(trees)
#for value in trees:
#    print(value)
print(answer)
    
