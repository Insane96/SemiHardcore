# Changelog

## 2.1.0
* Changed the default challenge
  * Player now starts with 2 health and 10 lives. Each time they die, they lose a life and gain 2 max health
* Now requires Forge 47.4.0+ and InsaneLib 1.21.20+
* Setting health through command no longer sends messages to the player (unless added)
* Added italian translation
* Fixed starting health not working

## 2.0.1
* Fixed command not working

## 2.0.0
* Updated to 1.20.1
* Added `/semihardcore <player> optout <true/false>` to opt out of the lives and max health changes 
* When out of lives, dying now instantly respawns you at the same location where you died
* Players are now informed when max health is lost
* Added a config option to cap the amount of max health modifier