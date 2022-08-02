package org.codecranachan.roster.util

class Quotes {
    companion object {
        private val quotes = listOf(
            "Yeth, mathter?",
            "What goeth around, cometh around... or thtopth.",
            "The game is afoot.",
            "So it begins.",
            "Open your eyes and then open your eyes again.",
            "Multiple exclamation marks,' he went on, shaking his head, 'are a sure sign of a diseased mind.",
            "On the Disc the gods dealt severely with atheists.",
            "Cake is not the issue here.",
            "SOD YOU, THEN, Death said.",
            "Gravity is a habit that is hard to shake off.",
            "The world is full of omens, and you picked the ones you liked.",
            "You are mistaking value for worth, I think.",
            "Ninety per cent of most magic merely consists of knowing one extra fact.",
            "Nine-tenths of the universe, in fact, is the paperwork.",
            "I know that time was made for men, not the other way around.",
            "Remember rule one.",
            "There is no doubt that being human is incredibly difficult and cannot be mastered in one lifetime.",
            "If you told humans what the future held, it wouldn’t.",
            "NO. THERE IS NO MORE TIME, EVEN FOR CAKE. FOR YOU, THE CAKE IS OVER. YOU HAVE REACHED THE END OF CAKE.",
            "Sometimes it's better to light a flamethrower than curse the darkness.",
            "Susan hated Literature. She'd much prefer to read a good book.",
            "The point was that people were dying and acts of incredibly stupid heroism were being performed.",
            "But this didn't *feel* like magic. It felt a lot older than that. It felt like music.",
            "Fate always wins...\nAt least when people stick to the rules.",
            "When many expect a mighty stallion they will find hooves on an ant.",
            "This isn't real life, this is *opera*.",
            "Always move fast. You never know what’s catching you up.",
            "What kind of man would put a known criminal in charge of a major branch of government? Apart from, say, the average voter.",
            "GNU Terry Pratchett",
            "A man is not dead while his name is still spoken."
        )

        fun getRandom(): String = quotes.random()
    }
}