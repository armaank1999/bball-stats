library(ggplot2)
nba2018teams=read.csv("/Users/armaan/Desktop/git/bball-stats/relativeOutput/2018TOT.csv")
defensemodel = lm(data = nba2018teams, DRtg ~ Pace + OppeFG. + OppTOV. + DRB. + OppFT.FG)
summary(defensemodel)
# As expected, everything matters. Faster paced teams tend to have worse defenses,
# even after accounting for the four factors, by a significant amount.
# Forcing more turnovers and getting defensive rebounds is good, while them making
# free throws and eFG more efficiently is bad.
offensemodelTS = lm(data = nba2018teams, ORtg ~ Pace + TS. + ORB. + TOV. + AST.)
offensemodelnoTS = lm(data = nba2018teams, ORtg ~ Pace + eFG. + FT.FG + ORB. + TOV. + AST.)
summary(offensemodelTS)
summary(offensemodelnoTS)
# The model involving TS% is better. Here, the pace term is not significant, but faster
# teams also tend to have worse offenses, even after accounting for the factors!
# AST% is not relevant after accounting for the other stuff. Let's see its correlation
# with the other variables.
cor(nba2018teams$AST.,nba2018teams$eFG.)*sd(nba2018teams$AST.)/sd(nba2018teams$eFG.)
cor(nba2018teams$AST.,nba2018teams$ORB.)*sd(nba2018teams$AST.)/sd(nba2018teams$ORB.)
cor(nba2018teams$AST.,nba2018teams$TS.)*sd(nba2018teams$AST.)/sd(nba2018teams$TS.)
cor(nba2018teams$AST.,nba2018teams$TOV.)*sd(nba2018teams$AST.)/sd(nba2018teams$TOV.)
# So highly positively correlated with eFG%, less with TS% because drawing fouls
# means there was no assist on that play. Interestingly, no negative correlation
# with turnover percentage, but team quality could be a confounding variable.
tovpctmodel = lm(data = nba2018teams, TOV. ~ ORtg + AST. + Pace)
summary(tovpctmodel)
# More assists and faster pace, more turnovers. Worse offense, more turnovers. Makes sense.
ggplot(data = nba2018teams, aes(x = X3PAr, y = X2P.)) + geom_point() + geom_smooth(method = lm)
ggplot(data = nba2018teams, aes(x = Pace, y = SRS)) + geom_point() + geom_smooth(method = lm)
ggplot(data = nba2018teams, aes(x = Pace, y = ORtg)) + geom_point() + geom_smooth(method = lm)
ggplot(data = nba2018teams, aes(x = Pace, y = DRtg)) + geom_point() + geom_smooth(method = lm)
ggplot(data = nba2018teams, aes(x = X3PAr, y = ORB.)) + geom_point() + geom_smooth(method = lm)
ggplot(data = nba2018teams, aes(x = X3PAr, y = FT.FG)) + geom_point() + geom_smooth(method = lm)
