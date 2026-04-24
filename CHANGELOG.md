Note: This version changes the conditional loading of market recipes. 

If you are creating and sharing custom data pack JSONs to add market recipes, you should now explicitly add an `is_group_enabled` load condition so that users can easily toggle your group of recipes. See [here](https://github.com/TwelveIterations/FarmingForBlockheads/commit/74eb480653e499a84e1a469ffe0a2c3fd3cf636d) for an example.

- Added `/farmingforblockheads list` command to view enabled/disabled groups
- Added `/farmingforblockheads enable` and `/farmingforblockheads disable` command for easy toggling of groups
- Added dedicated category for spawn eggs and soils, if enabled
- Added `is_group_enabled` resource load condition
- Changed Soul Sand to be grouped under `nether.soils`
- Fixed market block being relocatable by other mods, which would leave a broken half block behind
- Fixed incorrect translation keys for item blocks
