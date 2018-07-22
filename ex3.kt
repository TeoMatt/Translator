
//GENERAL
import simplenlg.features.*
import simplenlg.framework.*
import simplenlg.phrasespec.*
import simplenlg.lexicon.Lexicon

//ITALIAN
//importo feature italiane
import simplenlg.features.italian.*
//importo lessico italiano
import simplenlg.lexicon.italian.*
//importo il realizer francese che richiama i metodi
//realiseSyntax e realiseMorphology degli elementi linguistici
import simplenlg.realiser.Realiser
import java.io.InputStreamReader
import java.io.BufferedReader



    fun main(args: Array<String>) {

        /*########LESSICO##########*/
        val lexIta = ITXMLLexicon()

        /*########CREAZIONE FACTORY##########*/
        val factory = NLGFactory(lexIta)

        /*########CREAZIONE realiser##########*/

        val realiser = Realiser()
        //realiser.setDebugMode(true)
        var output: String?

        var le = ""

        val command = arrayOf("sh","/home/matteo/IdeaProjects/tlnEx3/src/script.sh")
        val process = Runtime.getRuntime().exec(command)
        val reader = BufferedReader(InputStreamReader(
                process.inputStream))
        val readerbis = BufferedReader(InputStreamReader(
                process.errorStream))
        var sbis = readerbis.readLine()
        while (sbis != null) {
            println("Script output error: $sbis")
            sbis = reader.readLine()
        }
        //while (reader.readLine() == null){}
        var s = reader.readLine()
        //println(s)
        while (s != null) {
            println("Script output: $s")
            le = le + s
            s = reader.readLine()
        }

        val logicExpression: String

        if(le != null){
            logicExpression = le
        }
        else{
            println("errore")
            return
        }


        var clause = factory.createClause()
        var sog: PhraseElement
        var ogg: PhraseElement

        val map = mapOf("mind" to "mente", "mind_pl" to "mente", "great" to "grande", "wonderful" to "meraviglioso", "truly" to "veramente", "one" to "persona", "child" to "bambino", "child_pl" to "bambino", "jedi" to "jedi", "jedi_pl" to "jedi", "force" to "forza", "knowledge" to "conoscenza", "war" to "guerra", "war_pl" to "guerra", "make" to "fare", "makes" to "fare", "use" to "usare", "uses" to "usare", "earth" to "terra")

        var t = logicExpression

        val varmap = mutableMapOf("subject" to t.split("""exists """.toRegex())[1].split("""\.""".toRegex())[0])

        //Devo capire se ho subito il soggetto o se ho ho anche l'aggettivo
        var totrim = ""
        var adjective: String
        var subject: String

        if (logicExpression.matches("""exists ${varmap["subject"]}\.\([a-z]+\([a-z]+_*[a-z]+\(${varmap["subject"]}\)\) &.*""".toRegex())){
            //C'è l'aggettivo
            totrim = logicExpression
            adjective = totrim.split("""exists ${varmap["subject"]}\.\(""".toRegex())[1].split("""\([a-z]+_*[a-z]+\(${varmap["subject"]}\).*""".toRegex())[0]
            println("Aggettivo: $adjective")

            subject = totrim.split("""exists ${varmap["subject"]}\.\([a-z]+\(""".toRegex())[1].split("""\(${varmap["subject"]}\)""".toRegex())[0]
            println("soggetto: $subject")

            sog = factory.createNounPhrase(map[subject])
            sog.addPreModifier(map[adjective])

            if (logicExpression.matches(""".*& all .*\.\($adjective\($subject\(.*\)\) -> \(${varmap["subject"]} =.*""".toRegex()))
              sog.setSpecifier("il")
            else
              sog.setSpecifier("un")

            //controllo se il soggetto è plurale
            if (subject.matches(""".*_pl""".toRegex())) sog.isPlural = true

            totrim = totrim.split("""exists ${varmap["subject"]}\.\($adjective\($subject\(${varmap["subject"]}\)\) & """.toRegex())[1]
            println(totrim)

        }else {
            //non c'è l'aggettivo
            totrim = logicExpression
            subject = totrim.split("""exists ${varmap["subject"]}\.\(""".toRegex())[1].split("""\(${varmap["subject"]}\)""".toRegex())[0]
            println("soggetto: $subject")

            sog = factory.createNounPhrase(map[subject])

            if (logicExpression.matches(""".*& all.*\.\($subject\(.*\) -> \(${varmap["subject"]} =.*""".toRegex()))
                sog.setSpecifier("il")
            else
                sog.setSpecifier("un")

            //controllo se il soggetto è plurale
            if (subject.matches(""".*_pl""".toRegex())) sog.isPlural = true

            totrim = totrim.split("""exists ${varmap["subject"]}\.\($subject\(${varmap["subject"]}\) & """.toRegex())[1]
            println(totrim)

        }

        clause.setSubject(sog)

        //Fine gestione soggetto, consideriamo ora l'oggetto

        if (!totrim.matches("""exists .*""".toRegex()) && !totrim.matches("""-exists .*""".toRegex())) {
            //Siamo nel caso in cui il verbo è sicuramente is è abbiamo un aggettivo
            clause.setVerb("essere")
            var negato = false

            var aggettivo: String
            var avverbio = ""
            if (totrim.matches("""-*[a-z]+\([a-z]+\(${varmap["subject"]}\)\).*""".toRegex())){

                if (totrim.matches("""-.*""".toRegex())) {
                    totrim = totrim.split("""-""".toRegex())[1]
                    avverbio = totrim.split("""\(""".toRegex())[0]
                    aggettivo = totrim.split("""\(""".toRegex())[1]
                    negato = true
                }
                else{
                    avverbio = totrim.split("""\(""".toRegex())[0]
                    aggettivo = totrim.split("""\(""".toRegex())[1]
                }
            }else{
                if (totrim.matches("""-.*""".toRegex())) {
                    totrim = totrim.split("""-""".toRegex())[1]
                    aggettivo = totrim.split("""\(""".toRegex())[0]
                    negato = true
                }else{
                    aggettivo = totrim.split("""\(""".toRegex())[0]
                }
            }

            ogg = factory.createAdjectivePhrase(map[aggettivo])
            if (!avverbio.equals("")){
                ogg.addPreModifier(map[avverbio])
            }

            if (negato) clause.verbPhrase.setFeature(Feature.NEGATED, true)
            clause.setObject(ogg)

        } else {

            var daNegare = false
            if (totrim.matches("""-exists .*""".toRegex())) {daNegare = true; totrim = totrim.substring(1)}

            //Siamo nel caso in cui ci sarà un of oppure un verbo
            //ci saranno una serie di exists, tanti quanti la cardinalità dell'of o del verbo seguente.
            var variabile1 = totrim.split("""exists """.toRegex())[1].split("""\.""".toRegex())[0]
            println(variabile1)
            var propvar1 =""
            var aggvar1 = ""
            if(totrim.matches("""exists $variabile1\.\([a-z]+_*[a-z]+\($variabile1\) &.*""".toRegex())){
                propvar1 = totrim.split("""exists $variabile1\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                println(propvar1)
                totrim = totrim.split("""exists $variabile1\.\($propvar1\($variabile1\) & """.toRegex())[1]
                println(totrim)
            }else{
                aggvar1 = totrim.split("""exists $variabile1\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                println(aggvar1)
                propvar1 = totrim.split("""exists $variabile1\.\($aggvar1\(""".toRegex())[1].split("""\(""".toRegex())[0]
                println(propvar1)
                totrim = totrim.split("""exists $variabile1\.\($aggvar1\($propvar1\($variabile1\)\) & """.toRegex())[1]
                println(totrim)
            }

            //Ora devo chiedermi cosa trovo dopo
            //potrei trovare un altro exist oppure potrei trovare un make o un of

            if(totrim.matches("""exists .*""".toRegex())){
                //dopo
                println("sono qui e stampo totrim")
                println(totrim)

                var variabile2 = totrim.split("""exists """.toRegex())[1].split("""\.""".toRegex())[0]
                println(variabile2)
                var aggvar2 = ""
                var propvar2 = ""

                if(totrim.matches("""exists $variabile2\.\([a-z]+_*[a-z]+\($variabile2\) &.*""".toRegex())){
                    propvar2 = totrim.split("""exists $variabile2\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                    //println(propvar1)
                    totrim = totrim.split("""exists $variabile2\.\($propvar2\($variabile2\) & """.toRegex())[1]
                    //println(totrim)
                }else{
                    aggvar2 = totrim.split("""exists $variabile2\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                    //println(aggvar1)
                    propvar2 = totrim.split("""exists $variabile2\.\($aggvar2\(""".toRegex())[1].split("""\(""".toRegex())[0]
                    //println(propvar1)
                    totrim = totrim.split("""exists $variabile2\.\($aggvar2\($propvar2\($variabile2\)\) & """.toRegex())[1]
                    //println(totrim)
                }
                /*var propvar2 = totrim.split("""exists $variabile2\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                println(propvar2)
                totrim = totrim.split("""exists $variabile2\.\($propvar2\($variabile2\) & """.toRegex())[1]
                println(totrim)*/

                //nel nostro caso il verbo è usare, ma genericamente abbiamo un DTV
                clause.setVerb(map[totrim.split("""\(""".toRegex())[0]])

                var complogg = factory.createNounPhrase(map[propvar2])

                if (aggvar2 != "") complogg.addPreModifier(map[aggvar2])

                if (logicExpression.matches(""".*& all.*\.\($propvar2\(.*\) -> \($variabile2 =.*""".toRegex()) || logicExpression.matches(""".*& all.*\.\($aggvar2\($propvar2\(.*\) -> \($variabile2 =.*""".toRegex()))
                    complogg.setSpecifier("il")
                else
                    complogg.setSpecifier("un")

                var percosa = factory.createNounPhrase(map[propvar1])

                if (aggvar1 != "") percosa.addPreModifier(map[aggvar1])

                if (logicExpression.matches(""".*& all.*\.\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex()) || logicExpression.matches(""".*& all.*\.\($aggvar1\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex()))
                    percosa.setSpecifier("il")
                else
                    percosa.setSpecifier("un")

                var perf = factory.createPrepositionPhrase("per", percosa)

                complogg.addPostModifier(perf)

                ogg = complogg

                //ogg.setFeature(Feature.NEGATED, true)
                if (daNegare) clause.verbPhrase.setFeature(Feature.NEGATED, true)
                if (propvar1.matches(""".*_pl""".toRegex())) ogg.isPlural = true

                clause.setObject(ogg)

            }else if(totrim.matches("""of\(${variabile1},.*""".toRegex())){

                totrim = totrim.split("""of\(${variabile1},${varmap["subject"]}\) & """.toRegex())[1]
                println(totrim)
                var appoggio: NPPhraseSpec

                if (logicExpression.matches(""".*& all [a-z][0-9]*\.\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex()) || logicExpression.matches(""".*& all [a-z][0-9]*\.\($aggvar1\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex()))
                    appoggio = factory.createNounPhrase("il", map[propvar1])
                else
                    appoggio = factory.createNounPhrase("un", map[propvar1])

                if(aggvar1 != ""){appoggio.addPreModifier(map[aggvar1])}

                if (propvar1.matches(""".*_pl""".toRegex())) appoggio.isPlural = true

                var dichi = factory.createPrepositionPhrase("di", appoggio)

                sog.addPostModifier(dichi)

                //Usando of, per la grammatica scritta, sappiamo per certo di dover usare il verbo essere
                println(totrim)
                if(!totrim.matches("""exists .*""".toRegex()) && !totrim.matches("""-exists .*""".toRegex())){
                    //siamo nel caso in cui siamo certi di avere il verbo essere
                    clause.setVerb("essere")
                    var negato = false

                    var aggettivo: String
                    var avverbio = ""
                    if (totrim.matches("""-*[a-z]+\([a-z]+\(${varmap["subject"]}\)\).*""".toRegex())){

                        if (totrim.matches("""-.*""".toRegex())) {
                            totrim = totrim.split("""-""".toRegex())[1]
                            avverbio = totrim.split("""\(""".toRegex())[0]
                            aggettivo = totrim.split("""\(""".toRegex())[1]
                            negato = true
                        }
                        else{
                            avverbio = totrim.split("""\(""".toRegex())[0]
                            aggettivo = totrim.split("""\(""".toRegex())[1]
                        }
                    }else{
                        if (totrim.matches("""-.*""".toRegex())) {
                            totrim = totrim.split("""-""".toRegex())[1]
                            aggettivo = totrim.split("""\(""".toRegex())[0]
                            negato = true
                        }else{
                            aggettivo = totrim.split("""\(""".toRegex())[0]
                        }
                    }

                    ogg = factory.createAdjectivePhrase(map[aggettivo])
                    if (!avverbio.equals("")){
                        ogg.addPreModifier(map[avverbio])
                    }

                    if (negato) clause.verbPhrase.setFeature(Feature.NEGATED, true)
                    //if (propvar1.matches(""".*_pl""".toRegex())) ogg.isPlural = true

                    clause.setObject(ogg)
                }else{
                    var daNegareDue = false
                    if (totrim.matches("""-exists .*""".toRegex())) {daNegareDue = true; totrim = totrim.substring(1)}

                    var variabile10 = totrim.split("""exists """.toRegex())[1].split("""\.""".toRegex())[0]
                    println(variabile10)
                    var propvar10 = ""
                    var aggvar10 = ""
                    if(totrim.matches("""exists $variabile10\.\([a-z]+_*[a-z]+\($variabile10\) &.*""".toRegex())){
                        propvar10 = totrim.split("""exists $variabile10\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                        println(propvar10)
                        totrim = totrim.split("""exists $variabile10\.\($propvar10\($variabile10\) & """.toRegex())[1]
                        println(totrim)
                    }else{
                        aggvar10 = totrim.split("""exists $variabile10\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                        println(aggvar10)
                        propvar10 = totrim.split("""exists $variabile10\.\($aggvar10\(""".toRegex())[1].split("""\(""".toRegex())[0]
                        println(propvar10)
                        totrim = totrim.split("""exists $variabile10\.\($aggvar10\($propvar10\($variabile10\)\) & """.toRegex())[1]
                        println(totrim)
                    }

                    if(totrim.matches("""exists .*""".toRegex())){
                        //dopo
                        println("sono qui e stampo totrim")
                        println(totrim)

                        var variabile20 = totrim.split("""exists """.toRegex())[1].split("""\.""".toRegex())[0]
                        println(variabile20)
                        var aggvar20 = ""
                        var propvar20 = ""
                        if(totrim.matches("""exists $variabile20\.\([a-z]+_*[a-z]+\($variabile20\) &.*""".toRegex())){
                            propvar20 = totrim.split("""exists $variabile20\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                            //println(propvar1)
                            totrim = totrim.split("""exists $variabile20\.\($propvar20\($variabile20\) & """.toRegex())[1]
                            //println(totrim)
                        }else{
                            aggvar20 = totrim.split("""exists $variabile20\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                            //println(aggvar1)
                            propvar20 = totrim.split("""exists $variabile20\.\($aggvar20\(""".toRegex())[1].split("""\(""".toRegex())[0]
                            //println(propvar1)
                            totrim = totrim.split("""exists $variabile20\.\($aggvar20\($propvar20\($variabile20\)\) & """.toRegex())[1]
                            //println(totrim)
                        }
                        /*var propvar20 = totrim.split("""exists $variabile20\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                        println(propvar20)
                        totrim = totrim.split("""exists $variabile20\.\($propvar20\($variabile20\) & """.toRegex())[1]
                        println(totrim)*/

                        //nel nostro caso il verbo è usare, ma genericamente abbiamo un DTV
                        clause.setVerb(map[totrim.split("""\(""".toRegex())[0]])

                        var complogg = factory.createNounPhrase(map[propvar20])

                        if(aggvar20 != ""){complogg.addPreModifier(map[aggvar20])}

                        if (logicExpression.matches(""".*& all.*\.\($propvar20\(.*\) -> \($variabile20 =.*""".toRegex()) || logicExpression.matches(""".*& all.*\.\($aggvar20\($propvar20\(.*\) -> \($variabile20 =.*""".toRegex()))
                            complogg.setSpecifier("il")
                        else
                            complogg.setSpecifier("un")

                        var percosa = factory.createNounPhrase(map[propvar10])

                        if(aggvar10 != ""){percosa.addPreModifier(map[aggvar10])}

                        if (logicExpression.matches(""".*& all.*\.\($propvar10\(.*\) -> \($variabile10 =.*""".toRegex()) || logicExpression.matches(""".*& all.*\.\($aggvar10\($propvar10\(.*\) -> \($variabile10 =.*""".toRegex()))
                            percosa.setSpecifier("il")
                        else
                            percosa.setSpecifier("un")

                        var perf = factory.createPrepositionPhrase("per", percosa)

                        complogg.addPostModifier(perf)

                        ogg = complogg

                        //ogg.setFeature(Feature.NEGATED, true)
                        if (daNegareDue) clause.verbPhrase.setFeature(Feature.NEGATED, true)
                        if (propvar10.matches(""".*_pl""".toRegex())) percosa.isPlural = true

                        clause.setObject(ogg)

                    }else{
                        println(totrim)
                        var tv = totrim.split("""\(""".toRegex())[0]
                        totrim = totrim.split("""$tv\(${varmap["subject"]},""".toRegex())[1]
                        println(totrim)
                        var propervar10 = totrim.split("""\($variabile10\)\)""".toRegex())[0]
                        println(propervar10)

                        //Nel nostro caso abbiamo make, ma genericamene abbiamo un TV
                        clause.setVerb(map[tv])
                        ogg = factory.createNounPhrase(map[propvar10])

                        if(aggvar10 != ""){ogg.addPreModifier(map[aggvar10])}

                        if (logicExpression.matches(""".*& all [a-z][0-9]*\.\($propvar10\(.*\) -> \($variabile10 =.*""".toRegex()) || logicExpression.matches(""".*& all [a-z][0-9]*\.\($aggvar10\($propvar10\(.*\) -> \($variabile10 =.*""".toRegex()) )
                            ogg.setSpecifier("il")
                        else
                            ogg.setSpecifier("un")
                        ogg.addPostModifier(map[propervar10])

                        if (daNegareDue) clause.verbPhrase.setFeature(Feature.NEGATED, true)
                        if (propvar10.matches(""".*_pl""".toRegex())) ogg.isPlural = true

                        clause.setObject(ogg)
                        println(clause.toString())

                    }
                }

            }else{
                println(totrim)
                var tv = totrim.split("""\(""".toRegex())[0]
                totrim = totrim.split("""$tv\(${varmap["subject"]},""".toRegex())[1]
                println(totrim)
                var propervar1 = totrim.split("""\($variabile1\)\)""".toRegex())[0]
                println(propervar1)

                //Nel nostro caso abbiamo make, ma genericamene abbiamo un TV
                clause.setVerb(map[tv])
                ogg = factory.createNounPhrase(map[propvar1])

                if(aggvar1 != ""){ogg.addPreModifier(map[aggvar1])}

                if (logicExpression.matches(""".*& all [a-z][0-9]*\.\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex()) || logicExpression.matches(""".*& all [a-z][0-9]*\.\($aggvar1\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex()))
                    ogg.setSpecifier("il")
                else
                    ogg.setSpecifier("un")
                ogg.addPostModifier(map[propervar1])

                if (daNegare) clause.verbPhrase.setFeature(Feature.NEGATED, true)
                if (propvar1.matches(""".*_pl""".toRegex())) ogg.isPlural = true

                clause.setObject(ogg)
                println(clause.toString())

            }

        }


        println(sog.noun)

        //clause.setSubject(sog)

        println(clause.subject.allFeatures.toList())
        println(clause.`object`.allFeatures.toList())
        println(clause.verbPhrase.allFeatures.toList())

        output = realiser.realiseSentence(clause)
        println("La frase è")
        println(output)

    }

