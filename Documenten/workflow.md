#Workflow

##Doelstelling
 1. Voldoen aan de specificatie in de opdracht.
 2. Zo stabiel mogelijke releases.
 3. Zo weinig mogelijk tijd steken in debuggen/merge conflicts/build errors/...
 4. Zo veel mogelijk tijd steken in nieuwe features ontwikkelend.

##Hoe?
Ik stel volgende workflow voor met elementen uit veelgebruikte patronen in de industrie:

 + **Agile Development (Kanban board + planning poker)**
 	1. Eerst bepalen we welke features nodig zijn voor de volgende release.
 	2. Deze vereisten krijgen een duidelijke specificatie. (Bij input A moet het programma X doen, ...)
 	3. Iedereen neemt/krijgt taken zodat iedereen ongeveer evenveel werk heeft. Iedereen markeert zijn taak als "actief" en zet er zijn naam bij. (Zodat er niet onnodig dubbel werk gedaan wordt, en dat we niets vergeten)
 	4. Eenmaal een taak klaar is, wordt deze gemarkeerd als "klaar".
 + **Interface-driven-development + Test-driven-development**
 	1. Werk aan de hand van de specificatie uit wat er allemaal nodig is om een feature te implementeren.
 	2. Maak interfaces (of UML diagramma) aan die op hoog niveau een structuur definiëren van hoe je code er zal uitzien.
 	3. Schrijf unit-tests per functie die bepalen hoe ieder functie zich zal moeten gedragen.
 	4. Implementeer de functies met code zodat ze voldoen aan de unit tests.
 	=> Als alles goed verlopen is, heb je nu code die voldoet aan de specificatie, goed gestructureerd is en geen bugs bevat.
\pagebreak
 + **Git Flow** (http://nvie.com/posts/a-successful-git-branching-model/)
 	
    Alle voordelen van git, maar vermijdt "merge hell" die anders ontstaat bij grote teams, geeft duidelijke scheiding tussen stabiele en beta versies.
 	- Wat is het?
 		- 2 permanente branches
 			- "master" branch
 			Bevat enkel "productie-klare", stabiele code. (= code die werkt naar specificatie, uitgebreid getest is en klaar om gereleased te worden.)
 			- "development" branch: 
 			Bevat versies van het programma met de laatste nieuwe features. De code in deze branch moet compileerbaar zijn, en de features moeten getest zijn, maar de code werkt nog niet volledig volgens specificatie en is nog niet genoeg getest om "stabiel" te worden genoemd.
 		- "feature branches": 
 			Deze branches zijn een "vertakking" van de development branch en dienen om een bepaalde feature uit te werken. Binnen een feature branch kunnen aanpassingen gemaakt worden, gecommit, gepusht en gepulled zonder conflicten met andere branches te creëren. Dit voorkomt "merge hell" waarbij continu aanpassingen van verschillende teamleden moeten worden samengevoegd.
 	- Hoe werkt het?
 		- Je wilt beginnen werken aan een feature voor het programma. Je maakt een nieuwe "feature branch" aan die aftakt van de "development branch" met een naam die de feature duidelijk beschrijft. In deze branch kan je aanpassingen maken aan de code en deze aanpassingen committen en pushen.
 		- Wanneer de nieuwe feature volledig volgens specificatie geimplementeerd en getest is, wordt deze gemerged met de development branch. Na het mergen verwijder je de feature branch.
 		- Eenmaal alle features op de specificatie geimplementeerd zijn, wordt het totale pakket getest. Pas als het totale pakket stabiel en klaar voor release is, wordt het gemerged met de "master branch". Dit is nu een nieuwe versie van het programma.
 + **Code reviews**

 	Om een feature branch te mergen met de development branch, dien je een "pull request in" op GitHub. De pull request blokkeert het mergen totdat iemand anders ook de code heeft getest en goedgekeurd. Hoe meer mensen een stuk code bekijken, hoe groter de kans dat bugs gevonden worden. Verder helpt het ook de kwaliteit van de code hoog te houden. Hoe langer je wacht, hoe moeilijker het wordt om de structuur van slechte code te verbeteren en de bugs er uit te halen omdat deze dan verankert zitten in het programma. Ook zorgt het ervoor dat er minstens 2 mensen weten hoe het stuk code werkt. Hetzelfde geldt voor de release branch.
 + **Travis CI**

 	Wanneer iemand een feature branch wil mergen met de development branch, of de development branch met de master, dan wordt er een pull request gemaakt. *Travis CI* zal automatisch de code in deze pull request compileren en testen op uitvoeren. Indien deze testen falen, is het niet mogelijk om de pull request te accepteren totdat deze fouten opgelost zijn. Dit verzekert de stabiliteit van het programma.

##Structuur programma:
 + 1 Java package met voorgemaakte code van assistenten
 + 3 Java packages met onze code
 	- `be.kuleuven.cs.robijn.gui`: Alle code voor de grafische interface
	- `be.kuleuven.cs.robijn.testbed`: Alle code voor de simulator (3D rendering, fysische simulatie, ...)
	- `be.kuleuven.cs.robijn.autopilot`: Alle code voor de autopilot (Interpretatie camerabeelden, aansturing motoren/vleugels, motion planning, ...)
	- `be.kuleuven.cs.robijn.common`: Algemene code die gemeenschappelijk is tussen de packages. (Bv. klassen voor vectorrekenen, matrices, ...)
 + Decoupling
 	- De code in `be.kuleuven.cs.robijn.testbed` moet losstaan van alle code in `be.kuleuven.cs.robijn.gui` en `be.kuleuven.cs.robijn.autopilot`
 	- De code in `be.kuleuven.cs.robijn.autopilot` moet losstaan van alle code in `be.kuleuven.cs.robijn.gui` en `be.kuleuven.cs.robijn.testbed`
 	- Het testbed en de autopilot kunnen enkel communiceren door de interface die we verkrijgen van de assistenten.
 	- Dit garandeerd dat we niet valsspelen door data door te geven die niet toegelaten is.
	  Verder maakt het het programma ook gemakkelijker testbaar, robuuster en flexibeler omdat aanpassingen in een package geen invloed hebben op code in andere packages.
	- Indien je toch een klasse wilt gebruiken uit een van deze packages, vraag je dan af of deze misschien niet beter in `be.kuleuven.cs.robijn.common` staat.

##Gebruikte libraries:
 - JavaFX: grafische interface
 - LWJGL (OpenGL): 3D rendering
 - ...