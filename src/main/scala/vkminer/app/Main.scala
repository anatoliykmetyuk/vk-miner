package vkminer.app

import subscript.language
import subscript.Predef._

import subscript.SubScriptApplication

object Main extends SubScriptApplication {
  script..
    live = initGui
           new MiningFrame
           
    initGui = javax.swing.UIManager.setLookAndFeel: "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
}