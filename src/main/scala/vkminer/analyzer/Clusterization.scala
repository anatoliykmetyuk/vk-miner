package vkminer.analyzer

import java.io.File

import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering._
import org.apache.spark.mllib.linalg._

import org.apache.commons.io._

import StringMethods._


class Clusterization(sc: SparkContext) {

  def clusterize(docs: Seq[String], k: Int = 15, vectorSize: Int = 300, iterations: Int = 1000): Seq[(String, Int)] = {
    // 1. Extract words from the documents
    val words = extractWords(docs)

    // 2. Create a bag of the most popular words
    val frequencies = wordFrequencies(words)
    val bag         = wordsBag(frequencies.map(_._2), vectorSize)

    // 3. Turn every document into a vector
    val activeDocs = docs.filter {_.split(" ").exists(bag.contains(_))}  // Each document must contain at least one word from the bag
    
    val docVectors = activeDocs.map {doc =>
      val vectorData: Seq[(Int, Double)] = doc
        .split(" ")                // Words
        .distinct                  
        .filter(bag.contains(_))   // Only those that are in the bag
        .map {w => bag(w) -> 1D}   // Turn each word into a pair of its index and 1

      Vectors.sparse(vectorSize, vectorData)
    }

    val docRDD = sc.makeRDD(docVectors)

    // 4. Train K-Means, predict the payload
    val model = KMeans.train(docRDD, k, iterations)
    println(s"Model generated with the error ${model.computeCost(docRDD)}")

    val clusters = model.predict(docRDD).collect

    // 5. Zip the original docs with their clusters
    activeDocs.zip(clusters)
  }

  def clustersToMap(c: Seq[(String, Int)]): Map[Int, Seq[String]] = c
    .groupBy {case (n, c) => c}                      // Group by the cluster index
    .map     {case (c, seq) => c -> seq.map(_._1)}   // Remove indices

  def serializeClusters(c: Map[Int, Seq[String]], dir: String, fileName: String = "clusters.txt") {
    val raw: Seq[Seq[String]] = c.map(_._2).toSeq
    val result: String = raw.map(_.mkString("\n")).mkString("\n\n\n=== NEW CLUSTER ===\n\n\n")
    FileUtils.writeStringToFile(new File(dir, fileName), result)
  }

}