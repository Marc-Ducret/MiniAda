### Projet de Compilation : Compilateur MiniADA ###

{ Choix du langage }

J'ai choisi le Java simplement pour la modularaité apportée par l'aspect Orienté 
Objet et parce qu'il m'est agréable de programmer en Java.


{ Utilisation }

Pour utiliser ce compilateur, un JRE (Java Runtime Environement) avec une
version supérieure à 1.8 est supposé.


{ Analyse Lexicale }

Sources : lexer.flex, package token, fonction lexicalAnalysis de Compiler


{ Analyse Syntaxique }

Sources : package syntax, fonctions syntaxAnalysis, matchSymbol, matchKeyword,
matchSymbol de Compiler

Ayant commencé cette partie avant la fin du cours la concernant et l'outil
correspondant pour Java semblant contraignant, j'ai décidé d'écrire le parseur
sans outil. 
Le parseur prend en entrée la liste des lexèmes (TokenList) et construit l'arbre
 syntaxique en la parcourant.
Cet arbre est constitué d'objets de types Instruction, Declaration et
Expression. Dans ceux-ci, la partie relative à l'analyse syntaxique est
constituée des méthodes "match****".
Dans chacune des classes concernant l'arbre syntaxique, on trouve des méthodes
"type****" concernant le typage, "execute****" ou "value****" concerant un
interpréteur réalisé à des fins de vérifications et "buildASM" concernant
la production de code.
La seule difficulté que j'ai rencontrée dans cette partie a été de concevoir dés
le début les conventions par lequels agir sur la TokenList. En effet, un manque
d'uniformité peut vite provoquer des comportements imprévus.

### Marc Ducret - 2016 ###