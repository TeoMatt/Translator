//GENERAL
import simplenlg.features.*
import simplenlg.framework.*
import simplenlg.phrasespec.*

import simplenlg.lexicon.italian.*
import simplenlg.realiser.Realiser


import java.io.InputStreamReader
import java.io.BufferedReader
import org.json.JSONObject


fun main(args: Array<String>) {

    val rootObject= JSONObject()
    val nsubj = JSONObject()
    val obj = JSONObject()

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
    //
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
        

        subject = totrim.split("""exists ${varmap["subject"]}\.\([a-z]+\(""".toRegex())[1].split("""\(${varmap["subject"]}\)""".toRegex())[0]
        

        nsubj.put("val", map[subject])
        nsubj.put("amod", map[adjective])

        
        if (logicExpression.matches(""".*& all .*\.\($adjective\($subject\(.*\)\) -> \(${varmap["subject"]} =.*""".toRegex())) {

            nsubj.put("det", "true")
        }else{

            nsubj.put("det", "false")
        }
        //controllo se il soggetto è plurale
        if (subject.matches(""".*_pl""".toRegex())) {
            nsubj.put("plural", "true")
        }else{
            nsubj.put("plural", "false")
        }

        
        totrim = totrim.split("""exists ${varmap["subject"]}\.\($adjective\($subject\(${varmap["subject"]}\)\) & """.toRegex())[1]
        

    }else {
        
        //non c'è l'aggettivo
        totrim = logicExpression
        subject = totrim.split("""exists ${varmap["subject"]}\.\(""".toRegex())[1].split("""\(${varmap["subject"]}\)""".toRegex())[0]
        


        nsubj.put("val", map[subject])

        
        if (logicExpression.matches(""".*& all.*\.\($subject\(.*\) -> \(${varmap["subject"]} =.*""".toRegex())) {
            nsubj.put("det", "true")
        }else{
            nsubj.put("det", "false")
        }

        //controllo se il soggetto è plurale
        if (subject.matches(""".*_pl""".toRegex())) {
            nsubj.put("plural", "true")
        }else{
            nsubj.put("plural", "false")
        }

        
        totrim = totrim.split("""exists ${varmap["subject"]}\.\($subject\(${varmap["subject"]}\) & """.toRegex())[1]
        

    }

    //Fine gestione soggetto, consideriamo ora l'oggetto

    if (!totrim.matches("""exists .*""".toRegex()) && !totrim.matches("""-exists .*""".toRegex())) {
        //Siamo nel caso in cui il verbo è sicuramente is è abbiamo un aggettivo


        rootObject.put("val", "essere")

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

        obj.put("scomp", map[aggettivo])
        if (!avverbio.equals("")){
            obj.put("adv_scomp", map[avverbio])
        }

        if (negato) {
            rootObject.put("neg", "true")
        }else{
            rootObject.put("neg", "false")
        }

    } else {

        var daNegare = false
        if (totrim.matches("""-exists .*""".toRegex())) {daNegare = true; totrim = totrim.substring(1)}

        //Siamo nel caso in cui ci sarà un of oppure un verbo
        //ci saranno una serie di exists, tanti quanti la cardinalità dell'of o del verbo seguente.
        var variabile1 = totrim.split("""exists """.toRegex())[1].split("""\.""".toRegex())[0]
        
        var propvar1 =""
        var aggvar1 = ""
        if(totrim.matches("""exists $variabile1\.\([a-z]+_*[a-z]+\($variabile1\) &.*""".toRegex())){
            propvar1 = totrim.split("""exists $variabile1\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
            
            totrim = totrim.split("""exists $variabile1\.\($propvar1\($variabile1\) & """.toRegex())[1]
            
        }else{
            aggvar1 = totrim.split("""exists $variabile1\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
            
            propvar1 = totrim.split("""exists $variabile1\.\($aggvar1\(""".toRegex())[1].split("""\(""".toRegex())[0]
            
            totrim = totrim.split("""exists $variabile1\.\($aggvar1\($propvar1\($variabile1\)\) & """.toRegex())[1]
            
        }

        //Ora devo chiedermi cosa trovo dopo
        //potrei trovare un altro exist oppure potrei trovare un make o un of

        if(totrim.matches("""exists .*""".toRegex())){
            //dopo
            
            

            var variabile2 = totrim.split("""exists """.toRegex())[1].split("""\.""".toRegex())[0]
            
            var aggvar2 = ""
            var propvar2 = ""

            if(totrim.matches("""exists $variabile2\.\([a-z]+_*[a-z]+\($variabile2\) &.*""".toRegex())){
                propvar2 = totrim.split("""exists $variabile2\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                //
                totrim = totrim.split("""exists $variabile2\.\($propvar2\($variabile2\) & """.toRegex())[1]
                //
            }else{
                aggvar2 = totrim.split("""exists $variabile2\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                //
                propvar2 = totrim.split("""exists $variabile2\.\($aggvar2\(""".toRegex())[1].split("""\(""".toRegex())[0]
                //
                totrim = totrim.split("""exists $variabile2\.\($aggvar2\($propvar2\($variabile2\)\) & """.toRegex())[1]
                //
            }
            /*var propvar2 = totrim.split("""exists $variabile2\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
            
            totrim = totrim.split("""exists $variabile2\.\($propvar2\($variabile2\) & """.toRegex())[1]
            */

            //nel nostro caso il verbo è usare, ma genericamente abbiamo un DTV
            rootObject.put("val", map[totrim.split("""\(""".toRegex())[0]])

            obj.put("val", map[propvar2])

            if (aggvar2 != "") {
                obj.put("amod", map[aggvar2])
            }

            if (logicExpression.matches(""".*& all.*\.\($propvar2\(.*\) -> \($variabile2 =.*""".toRegex()) || logicExpression.matches(""".*& all.*\.\($aggvar2\($propvar2\(.*\) -> \($variabile2 =.*""".toRegex())) {
                obj.put("det", "true")
            }else{
                obj.put("det", "false")
            }

            val prep = JSONObject()
            prep.put("noun", map[propvar1])

            if (aggvar1 != "") {
                prep.put("amod", map[aggvar1])
            }

            if (logicExpression.matches(""".*& all.*\.\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex()) || logicExpression.matches(""".*& all.*\.\($aggvar1\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex())){
                prep.put("det", "true")
            }else{
                prep.put("det", "false")
            }

            prep.put("p", "per")


            if (daNegare) rootObject.put("neg", "true") else rootObject.put("neg", "false")
            if (propvar1.matches(""".*_pl""".toRegex())) prep.put("plural", "true") else prep.put("plural", "false")

            obj.put("prep", prep)

        }else if(totrim.matches("""of\(${variabile1},.*""".toRegex())){

            totrim = totrim.split("""of\(${variabile1},${varmap["subject"]}\) & """.toRegex())[1]
            
            val prep = JSONObject()

            if (logicExpression.matches(""".*& all [a-z][0-9]*\.\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex()) || logicExpression.matches(""".*& all [a-z][0-9]*\.\($aggvar1\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex())) {
                prep.put("noun", map[propvar1])
                prep.put("det", "true")
            }else{
                prep.put("noun", map[propvar1])
                prep.put("det", "false")
            }

            if(aggvar1 != ""){
                prep.put("amod", map[aggvar1])
            }

            if (propvar1.matches(""".*_pl""".toRegex())) {
                prep.put("plural", "true")
            }else{prep.put("plural", "false")}

            prep.put("p", "di")
            nsubj.put("prep", prep)


            //Usando of, per la grammatica scritta, sappiamo per certo di dover usare il verbo essere
            
            if(!totrim.matches("""exists .*""".toRegex()) && !totrim.matches("""-exists .*""".toRegex())){
                //siamo nel caso in cui siamo certi di avere il verbo essere
                var negato = false

                rootObject.put("val", "essere")

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

                obj.put("scomp", map[aggettivo])
                if (!avverbio.equals("")){
                    obj.put("adv_scomp", map[avverbio])
                }

                if (negato) {
                    rootObject.put("neg", "true")
                }else{
                    rootObject.put("neg", "false")
                }

            }else{
                var daNegareDue = false
                if (totrim.matches("""-exists .*""".toRegex())) {daNegareDue = true; totrim = totrim.substring(1)}

                var variabile10 = totrim.split("""exists """.toRegex())[1].split("""\.""".toRegex())[0]
                
                var propvar10 = ""
                var aggvar10 = ""
                if(totrim.matches("""exists $variabile10\.\([a-z]+_*[a-z]+\($variabile10\) &.*""".toRegex())){
                    propvar10 = totrim.split("""exists $variabile10\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                    
                    totrim = totrim.split("""exists $variabile10\.\($propvar10\($variabile10\) & """.toRegex())[1]
                    
                }else{
                    aggvar10 = totrim.split("""exists $variabile10\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                    
                    propvar10 = totrim.split("""exists $variabile10\.\($aggvar10\(""".toRegex())[1].split("""\(""".toRegex())[0]
                    
                    totrim = totrim.split("""exists $variabile10\.\($aggvar10\($propvar10\($variabile10\)\) & """.toRegex())[1]
                    
                }

                if(totrim.matches("""exists .*""".toRegex())){
                    //dopo
                    
                    

                    var variabile20 = totrim.split("""exists """.toRegex())[1].split("""\.""".toRegex())[0]
                    
                    var aggvar20 = ""
                    var propvar20 = ""
                    if(totrim.matches("""exists $variabile20\.\([a-z]+_*[a-z]+\($variabile20\) &.*""".toRegex())){
                        propvar20 = totrim.split("""exists $variabile20\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                        //
                        totrim = totrim.split("""exists $variabile20\.\($propvar20\($variabile20\) & """.toRegex())[1]
                        //
                    }else{
                        aggvar20 = totrim.split("""exists $variabile20\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                        //
                        propvar20 = totrim.split("""exists $variabile20\.\($aggvar20\(""".toRegex())[1].split("""\(""".toRegex())[0]
                        //
                        totrim = totrim.split("""exists $variabile20\.\($aggvar20\($propvar20\($variabile20\)\) & """.toRegex())[1]
                        //
                    }
                    /*var propvar20 = totrim.split("""exists $variabile20\.\(""".toRegex())[1].split("""\(""".toRegex())[0]
                    
                    totrim = totrim.split("""exists $variabile20\.\($propvar20\($variabile20\) & """.toRegex())[1]
                    */

                    //nel nostro caso il verbo è usare, ma genericamente abbiamo un DTV
                    rootObject.put("val", map[totrim.split("""\(""".toRegex())[0]])

                    obj.put("val", map[propvar20])

                    if(aggvar20 != ""){
                        obj.put("amod", map[aggvar20])
                    }

                    if (logicExpression.matches(""".*& all.*\.\($propvar20\(.*\) -> \($variabile20 =.*""".toRegex()) || logicExpression.matches(""".*& all.*\.\($aggvar20\($propvar20\(.*\) -> \($variabile20 =.*""".toRegex())) {
                        obj.put("det", "true")
                    }
                    else{
                        obj.put("det", "false")
                    }

                    val prep = JSONObject()
                    prep.put("noun", map[propvar10])

                    if(aggvar10 != ""){
                        prep.put("amod", map[aggvar10])
                    }

                    if (logicExpression.matches(""".*& all.*\.\($propvar10\(.*\) -> \($variabile10 =.*""".toRegex()) || logicExpression.matches(""".*& all.*\.\($aggvar10\($propvar10\(.*\) -> \($variabile10 =.*""".toRegex())) {
                        prep.put("det", "true")
                    }else{
                        prep.put("det", "false")
                    }

                    prep.put("p", "per")


                    if (daNegareDue) rootObject.put("neg", "true") else rootObject.put("neg", "false")
                    if (propvar10.matches(""".*_pl""".toRegex())) prep.put("plural", "true") else prep.put("plural", "false")

                    obj.put("prep",prep)

                }else{
                    
                    var tv = totrim.split("""\(""".toRegex())[0]
                    totrim = totrim.split("""$tv\(${varmap["subject"]},""".toRegex())[1]
                    
                    
                    var propervar10 = totrim.split("""\($variabile10\)\)""".toRegex())[0]
                    

                    //Nel nostro caso abbiamo make, ma genericamene abbiamo un TV

                    rootObject.put("val", map[tv])
                    obj.put("val", map[propvar10])

                    if(aggvar10 != ""){
                        obj.put("amod", map[aggvar10])
                    }

                    if (logicExpression.matches(""".*& all [a-z][0-9]*\.\($propvar10\(.*\) -> \($variabile10 =.*""".toRegex()) || logicExpression.matches(""".*& all [a-z][0-9]*\.\($aggvar10\($propvar10\(.*\) -> \($variabile10 =.*""".toRegex()) ) {
                        obj.put("det", "true")
                    }
                    else{
                        obj.put("det", "false")
                    }
                    obj.put("amod_p", map[propervar10])


                    if (daNegareDue) rootObject.put("neg", "true") else rootObject.put("neg", "false")
                    if (propvar10.matches(""".*_pl""".toRegex())) obj.put("plural", "true") else obj.put("plural", "false")

                }
            }

        }else{
            
            
            var tv = totrim.split("""\(""".toRegex())[0]
            totrim = totrim.split("""$tv\(${varmap["subject"]},""".toRegex())[1]
            
            
            var propervar1 = totrim.split("""\($variabile1\)\)""".toRegex())[0]
            

            //Nel nostro caso abbiamo make, ma genericamene abbiamo un TV

            rootObject.put("val", map[tv])
            obj.put("val", map[propvar1])

            if(aggvar1 != ""){
                obj.put("amod", map[aggvar1])
            }

            if (logicExpression.matches(""".*& all [a-z][0-9]*\.\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex()) || logicExpression.matches(""".*& all [a-z][0-9]*\.\($aggvar1\($propvar1\(.*\) -> \($variabile1 =.*""".toRegex())){
                obj.put("det", "true")
            }
            else{
                obj.put("det", "false")
            }
            obj.put("amod_p", map[propervar1])

            if (daNegare) rootObject.put("neg", "true") else rootObject.put("neg", "false")
            if (propvar1.matches(""".*_pl""".toRegex())) obj.put("plural", "true") else obj.put("plural", "false")
        }

    }

    rootObject.put("nsubj", nsubj)
    rootObject.put("obj", obj)
    println(rootObject.toString(8))

    println(realize(rootObject))

}


fun realize(sentence: JSONObject): String{
    /*########LESSICO##########*/
    val lexIta = ITXMLLexicon()

    /*########CREAZIONE FACTORY##########*/
    val factory = NLGFactory(lexIta)

    /*########CREAZIONE realiser##########*/

    val realiser = Realiser()
    //realiser.setDebugMode(true)
    var output: String?

    var clause = factory.createClause()
    clause.setVerb(sentence.get("val"))
    var sog: PhraseElement

    var nsubj = JSONObject(sentence.get("nsubj").toString())

    sog = factory.createNounPhrase(nsubj.get("val").toString())
    if (nsubj.get("det").toString() == "true") sog.setSpecifier("il") else sog.setSpecifier("un")

    try {
        sog.addPreModifier(nsubj.get("amod").toString())
    }catch (e: Exception){}
    try {
        if( nsubj.get("plural").toString() == "true") sog.isPlural = true
    }catch (e: Exception){}

    var ogg: PhraseElement
    var obj = JSONObject(sentence.get("obj").toString())

    try {
        ogg = factory.createNounPhrase(obj.get("val").toString())

        try {
            if (obj.get("det").toString() == "true") ogg.setSpecifier("il") else ogg.setSpecifier("un")
        }catch (e: Exception){}

        try {
            ogg.addPreModifier(obj.get("amod").toString())
        }catch (e: Exception){}
        try {
            if( obj.get("plural").toString() == "true") ogg.isPlural = true
        }catch (e: Exception){}
        try {
            ogg.addPostModifier(obj.get("amod_p").toString())
        }catch (e: Exception){}
    }catch (e: Exception){
        ogg = factory.createAdjectivePhrase(obj.get("scomp").toString())

        try {
            ogg.addPreModifier(obj.get("adv_scomp").toString())
        }catch (e: Exception){}
        try {
            if( obj.get("plural").toString() == "true") ogg.isPlural = true
        }catch (e: Exception){}
    }

    //Preposizioni
    try {
        var prep = JSONObject(nsubj.get("prep").toString())
        var app = factory.createNounPhrase(prep.get("noun").toString())
        if (prep.get("det").toString() == "true") app.setSpecifier("il") else app.setSpecifier("un")
        try {
            if( prep.get("plural").toString() == "true") app.isPlural = true
        }catch (e: Exception){}
        try {
            app.addPreModifier(prep.get("amod").toString())
        }catch (e: Exception){}
        var dichi = factory.createPrepositionPhrase(prep.get("p").toString(), app)
        sog.addPostModifier(dichi)
    }catch (e: Exception){}

    try {
        var prep = JSONObject(obj.get("prep").toString())
        var app = factory.createNounPhrase(prep.get("noun").toString())
        if (prep.get("det").toString() == "true") app.setSpecifier("il") else app.setSpecifier("un")
        try {
            if( prep.get("plural").toString() == "true") app.isPlural = true
        }catch (e: Exception){}
        try {
            app.addPreModifier(prep.get("amod").toString())
        }catch (e: Exception){}
        var percosa = factory.createPrepositionPhrase(prep.get("p").toString(), app)
        ogg.addPostModifier(percosa)
    }catch (e: Exception){}

    clause.setSubject(sog)
    clause.setObject(ogg)
    output = realiser.realiseSentence(clause)
    return output
}

