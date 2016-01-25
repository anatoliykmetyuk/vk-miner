package vkminer.util

import subscript.language
import subscript.Predef._
import subscript.objectalgebra._

import subscript.vm.AAHappened
import subscript.vm.model.callgraph.CallGraphNode

object Scripts {

  def absorbAAHappened(target: => CallGraphNode)(implicit there: CallGraphNode) {
    val se = there.scriptExecutor
    se.msgHandlers.sInsert {
      case AAHappened(n, c, _) if (n eq there) && (c eq target) => se.msgQueue.dequeue(Int.MinValue)
    }
  }

  class ValueTrigger extends Trigger {
    var buffer: Any = null

    def triggerWithValue(v: Any) {
      buffer = v
      trigger
    }

    override script lifecycle = super.lifecycle ^buffer
  }

}
