### 1.5.0 Frums
Rewritten auto-assign to be usable both as autofiller and solution verifier, this means it should no longer come up with solutions that are impossible to split up into squads.
The downside to this is that it makes finding a solution somewhat slower.

- With druid being in a weird spot the squadmaker prefers but not requires having a druid, having none or having multiple are punished and those solutions are less likely to be picked. In case of a shortage of other healers it might still occur though.

### 1.4.0 Frums
* Added the ability to add `forbidden` roles to squads to prevent them from being used in solutions
* Added the ability to mark roles as `overflowable` which means you can never have too many of them
* Fixed a bug in the algorithm that always used the first possible (and only the first possible) role to match a criteria even though you had multiple roles that could fill that, this prevented some solutions from being created 

### 1.3.1 Frums
* Fixed a bug in the algorithm that attempted to use roles that overfilled boons, resulting in no result in very specific setups while there would be possibilities
* Fixed the DPS leftover check that made the squadmaker require more players than it actually needs

### 1.3 Frums
* Restructured code quite a bit
* Updated the squad building algorithm to attempt to build squads more efficiently
* Added discordping to the export output

### 1.2 Moon
* Improvement: Roles and Squads are read from .csv files.
* Update: Added discordPing variable to Player object and updated csv import and export to support it.
* Update: Updated player csv import for new file format.
* New Feature: When saving a squad, all players with the same GW2 account will be removed. This is to allow multiple player objects of the same player with different roles depending on tier.

### 1.1 Moon
* FIX: Manually assigning a role no longer breaks the automatic squad assignment.
* FIX: Squadcounter in export file should be correct now.
* FIX: Training level signups are saved in the export now.
* FIX~ish: Auto-fill trainees button should be a bit more reliable now.
* Improvement: Roles can be easily added and removed now.
* Improvement: Added some new roles to Commander sheet and export.
* Improvement: Commander Role selection table is now dynamically generated from the available roles.
* Improvement: Manual Role assignment preselects a role if you have filtered for a role now ~Thanks Matthias!
* New Feature: Training Levels are dynamic now. Dropdown Menu will detect training levels in the import file and show all training levels available.
* New Feature: The SquadmakerTM supports multiple Squad Configurations and Special Roles now.
* New Feature: You can select what squads types are allowed for this particular scenario now.

ToDo:
load roles and squads from csv instead of hardcoded in java file -> done in 1.2
allow the same player to sign up with different roles for different training levels - idea: when saving squads, remove all other instances of same player based on gw2 id -> done in 1.2
first two trainers per squad, then trainees, then remaining trainers
on Result view, make squad type selectable
