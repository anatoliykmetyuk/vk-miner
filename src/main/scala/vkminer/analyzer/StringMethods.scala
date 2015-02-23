package vkminer.analyzer

object StringMethods {

  val transliteMap = Map(
    ('а',"a"),
    ('б',"b"),
    ('в',"v"),
    ('г',"g"),
    ('д',"d"),
    ('е',"e"),
    ('ж',"zh"),
    ('з',"z"),
    ('и',"i"),
    ('й',"y"),
    ('к',"k"),
    ('л',"l"),
    ('м',"m"),
    ('н',"n"),
    ('о',"o"),
    ('п',"p"),
    ('р',"r"),
    ('с',"s"),
    ('т',"t"),
    ('у',"u"),
    ('ф',"f"),
    ('х',"h"),
    ('ц',"ts"),
    ('ч',"ch"),
    ('ш',"sh"),
    ('щ',"shch"),
    ('ъ',""),
    ('ы',"y"),
    ('ь',""),
    ('э',"e"),
    ('ю',"yu"),
    ('я',"ya"),

    ('є',"ye"),
    ('і',"i"),
    ('ї',"yi")
  )

  def alphaNum(c: Char) =
    (c >= 'a' && c <= 'z') ||
    (c >= 'а' && c <= 'я') ||
    Set('є', 'і', 'ї', ' ').contains(c)

  def translite(str: String) = str.map {c => transliteMap.get(c).getOrElse(c)}.mkString

  // Turns a sentence to a lower case, only alphanumerics are allowed, words are separated by
  // only one space.
  def normalizeSentence(str: String) =
    translite(str.toLowerCase.filter(alphaNum)).split(" ").filter(_ != "").mkString(" ")

  // Given a sequence of documents, returns a sequence of the words
  // these documents are made of.
  def extractWords(docs: Seq[String]): Seq[String] =
    docs.flatMap(_.split(" ")).filter(_.size >= 3)

  // Given a sequence of words, computes the number of occurrences of each word
  // in the sequence. The resulting Seq[(Int, String)] is sorted so that most
  // frequent words go first.
  def wordFrequencies(words: Seq[String]): Seq[(Int, String)] = 
    words.toSet.toList.map {w: String => words.count(_ == w) -> w}.sorted.reverse

  // Given a sequence of words, creates a mapping between the words
  // and their indices.
  def wordsBag(words: Seq[String], size: Int): Map[String, Int] =
    words.take(size).zipWithIndex.toMap

  // Filters out the documents that don't countain any of the featured words.
  def activeDocs(docs: Seq[String], featuredWords: Seq[String]): Seq[String] =
    docs.filter(_.split(" ").exists(featuredWords.contains(_)))
}