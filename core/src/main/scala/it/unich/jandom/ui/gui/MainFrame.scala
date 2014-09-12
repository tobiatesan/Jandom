/**
 * Copyright 2013 Gianluca Amato
 *
 * This file is part of JANDOM: JVM-based Analyzer for Numerical DOMains
 * JANDOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JANDOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty ofa
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JANDOM.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unich.jandom.ui.gui

import java.awt.event.{ InputEvent, KeyEvent }

import scala.swing._

import javax.swing.KeyStroke
import javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE

class MainFrame extends Frame {

  object Mode extends Enumeration {
    type Mode = Value
    val Random, Asm, Soot = Value
  }

  import Mode._

  private var realMode: Mode = Mode.Random

  val jandomEditorPane = new JandomEditorPane(this)
  val asmEditorPane = new ASMEditorPane(this)
  val sootEditorPane = new SootEditorPane(this)
  val outputPane = new OutputPane
  val parametersPane = new ParametersPane
  var currentEditorPane: TargetPane = jandomEditorPane
  val tabbedPane = new TabbedPane {
    pages += new TabbedPane.Page("Editor", currentEditorPane)
    pages += new TabbedPane.Page("Output", new ScrollPane(outputPane))
    pages += new TabbedPane.Page("Parameters", parametersPane)
  }
  var buttonGroups: ButtonGroup = null

  /**
   * This is the Action to invoke when user wants to quit the application
   */
  val quitAction = new Action("Quit") {
    accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK))
    def apply() {
      if (currentEditorPane.ensureSaved) sys.exit(0)
    }
  }

  /**
   * This is the action to invoke when the user selects the About Box dialog
   */
  val aboutAction = new Action("About") {
    def apply() {
      AboutDialog.visible = true
    }
  }

  /**
   * This is the action to invoke when the user press the Analyze button
   */
  val analyzeAction = new Action("ANALYZE!") {
    def apply() {
      currentEditorPane.analyze match {
        case Some(output) =>
          outputPane.text = outputPane.text + output
          tabbedPane.selection.index = 1
        case None =>
      }
    }
  }

  val randomAction: Action = new Action("Random") {
    toolTip = "Analysis of C-style programs"
    def apply() {
      mode = Random
    }
  }

  val asmAction: Action = new Action("ASM") {
    toolTip = "Analysis of Java bytecode thorugh the ASM library"
    def apply() {
      mode = Asm
    }
  }

  val sootAction: Action = new Action("Soot") {
    toolTip = "Analysis of Java bytecode thorugh the Soot library"
     def apply() {
      mode = Soot
    }
  }

  init()

  def init() {
    title = "Jandom"

    contents = new BorderPanel {
      val analyzeButton = new Button(analyzeAction)
      layout(tabbedPane) = BorderPanel.Position.Center
      layout(analyzeButton) = BorderPanel.Position.South
    }
    setMenuBar()
    bounds = new Rectangle(100, 100, 800, 600)
    peer.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)

  }

  def setFileMenu() {
    val fileMenu = menuBar.menus(0)
    fileMenu.contents.clear()
    fileMenu.contents ++= currentEditorPane.fileMenuItems
    if (!currentEditorPane.fileMenuItems.isEmpty)
      fileMenu.contents += new Separator
    fileMenu.contents += new MenuItem(quitAction)
  }

  def setEditMenu() {
    val editMenu = menuBar.menus(1)
    editMenu.contents.clear()
    if (currentEditorPane.editMenuItems.isEmpty)
      editMenu.enabled = false
    else
      editMenu.enabled = true
    editMenu.contents ++= currentEditorPane.editMenuItems
  }

  def setMenuBar() {
    val randomMode = new RadioMenuItem("")
    randomMode.action = randomAction
    val asmMode = new RadioMenuItem("")
    asmMode.action = asmAction
    val sootMode = new RadioMenuItem("")
    sootMode.action = sootAction

    buttonGroups = new ButtonGroup(randomMode, asmMode, sootMode)
    menuBar = new MenuBar {
      contents += new Menu("File")
      contents += new Menu("Edit")
      contents += new Menu("Tool") {
        contents += new MenuItem(outputPane.clear)
        contents += new Separator
        contents ++= buttonGroups.buttons
      }
      contents += new Menu("Help") {
        contents += new MenuItem(aboutAction)
      }
    }
    sootMode.peer.doClick
  }

  /**
   * Closing the frame causes the program to exit
   */
  override def closeOperation() {
    quitAction()
  }

  def mode = realMode

  def mode_= (m: Mode) {
    realMode = m
    currentEditorPane = m match {
      case Asm => asmEditorPane
      case Soot => sootEditorPane
      case Random => jandomEditorPane
    }
    tabbedPane.pages(0).content = currentEditorPane
    // repaint is needed to avoid corruption in display
    tabbedPane.repaint
    setFileMenu()
    setEditMenu()
    currentEditorPane.select()
  }
}