package vkminer.app

import subscript.language
import subscript.Predef._

import subscript.objectalgebra._
import subscript.swing._
import subscript.swing.Scripts._

import subscript.vm.model.callgraph.CallGraphNode

import vkminer.VkApi
import vkminer.util.Scripts._
import vkminer.serialize._
import vkminer.dom._

import java.awt.Point
import javax.swing.BorderFactory

import scala.swing._
import scala.swing.BorderPanel.Position._

import java.io.File


class MiningFrame extends Frame with FrameProcess
                     with MiningFrameLogic
                     with VkEnvironment
                     with GexfSerializerComponent
                     with UniversitiesSerializerComponent
                     with VkEnvironmentSettings {

  title = "VK Mining"
  location = new Point(300, 300)

  // Parameters
  val idLabel         = new Label("Id")
  val idText          = new TextField
  val wallCheckBox    = new CheckBox("Wall analysis")
  val parametersPanel = new BorderPanel {
    layout(idLabel)      = West
    layout(idText )      = Center
    layout(wallCheckBox) = East
  }

  // Output
  val outputLabel = new Label("Output file")
  val outputBtn   = new Button("Select") {enabled = false}
  val outputPanel = new BorderPanel {
    layout(outputLabel) = Center
    layout(outputBtn  ) = East
  }

  // Control panel
  val personBtn    = new Button("Person"   ) {enabled = false}
  val communityBtn = new Button("Community") {enabled = false}
  val cancelBtn    = new Button("Cancel"   ) {enabled = false}
  val controlPanel = new GridPanel(1, 3) {contents ++= Seq(
    personBtn, communityBtn, cancelBtn
  )}

  // Progress bars
  val iterationProgress = new ProgressBar {label = "Iteration"; labelPainted = true}
  val userProgress      = new ProgressBar {label = "User"     ; labelPainted = true}
  val wallProgress      = new ProgressBar {label = "Wall"     ; labelPainted = true}
  
  // Main panel
  val mainPanel = new GridPanel(6, 1) {
    contents ++= Seq(
      parametersPanel
    , controlPanel
    , outputPanel

    , iterationProgress
    , userProgress
    , wallProgress
    )

    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
  }

  contents = mainPanel


}

trait MiningFrameLogic {this: MiningFrame =>

  var outputFile: Option[File] = None

  script..
    live = controls ...

    controls = + outputSeq personSeq communitySeq

    outputSeq = outputBtn selectFile ~~(null)~~> [+]
                                    +~~(file: File)~~> [
      let outputFile = Some(file)
      let outputLabel.text = file.getAbsolutePath
    ]

    personSeq    = processingSeq(personBtn   )
    communitySeq = processingSeq(communityBtn)

    processingSeq(btn: Button) =
      var target: CallGraphNode = null
      @absorbAAHappened(target): [
        @{target = here}: guard: idText, {() => !idText.text.isEmpty}
        btn
        {..}
      ]

    selectFile =  val chooser = new FileChooser
                  if chooser.showSaveDialog(null) == FileChooser.Result.Approve then [
                    val extension = chooser.selectedFile.getAbsolutePath.reverse.takeWhile(_ != '.').reverse
                    if extension != "gexf" then ^new File(chooser.selectedFile.getAbsolutePath + ".gexf") else ^chooser.selectedFile
                  ]
}

trait VkEnvironmentSettings {this: MiningFrame =>
  val workingDirectory = "/Users/anatolii/Desktop"
  val api              = new VkApi(None)
  val universities     = UniversitiesSerializer.deserialize("universities")
}