# BBall Stats

### Part 1: Historical Data + Analysis

All things change over time, and the NBA is no exception. When talking about two players from different eras, there seems to be no objective way to compare them.
And not only have their been continuous advancements in medicine, training, nutrition, etc, but there have also been discrete rule changes at many points in NBA history.

Some people will bring up rules like hand-checking, but can we quantify how much such changes affect the NBA? Well, yes.
This was the most simple part of this project. I went to basketball reference, copied their league history table, then added some more rows (like TS%, 3PAr, etc) and then made graphs of the resulting data. Super simple, and it showed clear trends that make sense given historical context of rule changes.

All this is found in the avgs by season.xlsx file in this repository. I learned teams play worse offensively in lockout seasons, that hand-checking's impact is statistically significant, and that offensive rebounding is somehow dying.
Obviously, just looking at numbers is no use - it would be nice to figure out why these trends happened - but finding the trend has to come before finding the reason.

But then I wanted to compare players from different eras. This was possible, but I would have to individually go to each player's page, copy their table, put it in a CSV file, then run the SeasonParser file to compare their achievements with that year's average player.
It was fun, but a lot of busy work. So I decided that it would be nice to be able to read from basketball-reference by simply calling a function. So that leads to part 2 of the project.

### Part 2: Creating an API

I decided it would be best to copy from each teams page, as that is easier to do than find every player season link, and then issues with players who go to the ABA or miss a whole season, etc, occur.
However, players who get traded mid season won't have their full stats taken into account, but that's a problem with parsing the player page anyway.
I decided to store each of these TeamSeasons in a TeamSeason object, which each get saved to CSV.
