package vkminer.app

import subscript.language
import subscript.Predef._

import subscript.objectalgebra._
import subscript.swing._
import subscript.swing.Scripts._

import java.awt.Point
import javax.swing.BorderFactory

import scala.swing._
import scala.swing.BorderPanel.Position._


class MiningFrame extends Frame with FrameProcess {
  
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
  val mainPanel = new GridPanel(5, 1) {
    contents ++= Seq(
      parametersPanel
    , controlPanel

    , iterationProgress
    , userProgress
    , wallProgress
    )

    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
  }

  contents = mainPanel

  script..
    live = {..}
}