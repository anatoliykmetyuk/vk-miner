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
import vkminer.strategies._

import java.awt.Point
import javax.swing.BorderFactory

import scala.swing._
import scala.swing.BorderPanel.Position._

import java.io.File


class MiningFrame extends Frame with FrameProcess
                     with MiningFrameLogic
                     with VkEngine {
  title     = "VK Mining"
  location  = new Point(300, 300)
  resizable = false

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
  val iterationProgressText = "Users"
  val userProgressText      = "Current User"
  val wallProgressText      = "Wall"

  val iterationProgress = new ProgressBar {label = iterationProgressText; labelPainted = true; min = 0}
  val userProgress      = new ProgressBar {label = userProgressText     ; labelPainted = true; min = 0}
  val wallProgress      = new ProgressBar {label = wallProgressText     ; labelPainted = true; min = 0}
  
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

trait VkEngine {this: MiningFrame =>
  val environment = new VkEnvironment with XmlSerializerComponent
                                      with GexfSerializerComponent
                                      with UniversitiesSerializerComponent {
    val workingDirectory = null
    val api = new VkApi(None)
    val universities = UniversitiesSerializer.deserialize(getClass.getResourceAsStream("/universities.csv"))
  }

  val depthTrigger     = new ValueTrigger
  val iterationTrigger = new ValueTrigger
  val userTrigger      = new ValueTrigger
  val wallTrigger      = new ValueTrigger

  import vkminer.strategies.generic.FriendsExpansion._
  val listener: (String, Int, Int) => Unit = {(tpe, i, max) =>
    if (Thread.currentThread.isInterrupted) throw new InterruptedException
    val trigger = tpe match {
      case DEPTH     => depthTrigger    
      case ITERATION => iterationTrigger
      case USER      => userTrigger     
      case WALL      => wallTrigger

      case x => println("LEAK! " + x); null
    }

    trigger.triggerWithValue((i, max))
  }

  val ego = new FullEgoGroup {
    override type E   = environment.type     
    override val e: E = environment

    listeners :+= listener
  }

  val com = new Community {
    override type E   = environment.type
    override val e: E = environment

    listeners :+= listener
  }
}

trait MiningFrameLogic {this: MiningFrame =>
  import environment._

  val PERSON    = "person"
  val COMMUNITY = "community"

  var outputFile: Option[File] = None

  script..
    live = controls ...

    controls = + outputSeq personSeq communitySeq

    outputSeq = outputBtn selectFile ~~(null)~~> [+]
                                    +~~(file: File)~~> [
      let outputFile = Some(file)
      let outputLabel.text = file.getAbsolutePath
    ]

    selectFile =  val chooser = new FileChooser
                  if chooser.showSaveDialog(null) == FileChooser.Result.Approve then [
                    val extension = chooser.selectedFile.getAbsolutePath.reverse.takeWhile(_ != '.').reverse
                    if extension != "gexf" then ^new File(chooser.selectedFile.getAbsolutePath + ".gexf") else ^chooser.selectedFile
                  ]

    personSeq    = processingSeq(PERSON   )
    communitySeq = processingSeq(COMMUNITY)


    processingSeq(which: String) =
      var target: CallGraphNode = null
      @absorbAAHappened(target): [
        @{target = here}: guard: idText, {() => !idText.text.isEmpty && outputFile.isDefined}
        if which == PERSON then personBtn else communityBtn
        [[process(which) ~~(g: Graph)~~> serialize(g)
                       +~/~(null    )~~> [+]] || monitor] / cancelBtn
      ]

    process(which: String) =
      // Clean-up
      let iterationProgress.value = 0
      let userProgress     .value = 0
      let wallProgress     .value = 0

      if which == PERSON then {* ego(idText.text, 1, true, wallCheckBox.selected) *}^
      else                    {* com(idText.text,          wallCheckBox.selected) *}^
    
    monitor = && monitorTrigger(iterationTrigger, iterationProgress, iterationProgressText)
                 monitorTrigger(userTrigger     , userProgress     , userProgressText     )
                 monitorTrigger(wallTrigger     , wallProgress     , wallProgressText     )

    monitorTrigger(trigger: ValueTrigger, bar: ProgressBar, name: String) =
      [trigger ~~((i: Int, max: Int))~~> @gui: {!
        bar.label = name + " " + i + "/" + max
        bar.value = i
        bar.max   = max 
      !}] ...

    serialize(g: Graph) = GexfSerializer.serialize(g, outputFile.get)
}