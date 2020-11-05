# Implemetation basique d'une machine de turing

## Utilisation

L'émulateur se lance par la comande (après compilation)
```shell
java Main -machine /path/ [-tape /path/] [-display] [-debug]
```

Une aide est disponible avec:
```shell
> java Main -h
Usage: The program expect the following arguments:
  [Mandatory] | -machine [path] : path to the turing machine to emulate
  [Optionnal] | -tape [path]    : path to initial tape state
  [Optionnal] | -display        : the emulator will display detailed information during the excution
  [Optionnal] | -debug          : the emulator will display the debug information
```

## Fonctionnement

L'émulateur de machine de turing charge la machine à émuler depuis un fichier texte.
3 parametres sonts obligatoires:
- state_number: il represente le nombre d'état qu'aura notre machine de turing. (par exemple pour 3, la machine attendra des états parmis s0, s1, s2)
- accepting_states: il donne les états finaux dans lesquels on peut considerer que le mot d'entré est acepté
- transtions: indique à l'émulateur les transition de la machine

Un parametre optionel peut être fournit:
- tape_offset: il permet de déplacer la tête de lecture sur avant de débuter l'excution. Cela peut permetre positionner la tête de lecture au milieux d'un mot.

L'emulateur peut aussi prendre en entré un mot sous la forme d'un fichier texte.
Ce dernier sera alors positionné à partir de la position 0 sur le ruban positif.

Une fois les parametres chargés, la machine de turing s'éxcutera jusqu'à ce quelle rencontre un etat et une valeur pour lesquels aucune transition n'a été definie. Elle s'arretera alors est acceptera le mot en fonction de son état courant.