package jadx.core.dex.visitors.typeinference;

import java.util.*;

import jadx.core.dex.instructions.*;
import jadx.core.dex.instructions.args.*;
import jadx.core.dex.nodes.*;
import jadx.core.dex.visitors.*;
import jadx.core.utils.exceptions.*;

public class TypeInference extends AbstractVisitor {

	@Override
	public void visit(MethodNode mth) throws JadxException {
		if (mth.isNoCode()) {
			return;
		}
		DexNode dex = mth.dex();
		for (SSAVar var : mth.getSVars()) {
			// inference variable type
			ArgType type = processType(dex, var);
			if (type == null) {
				type = ArgType.UNKNOWN;
			}
			var.setType(type);

			// search variable name
			String name = processVarName(var);
			var.setName(name);
		}

		// fix type for vars used only in Phi nodes
		for (SSAVar sVar : mth.getSVars()) {
			PhiInsn phi = sVar.getUsedInPhi();
			if (phi != null) {
				processPhiNode(phi);
			}
		}
	}

	private static ArgType processType(DexNode dex, SSAVar var) {
		RegisterArg assign = var.getAssign();
		List<RegisterArg> useList = var.getUseList();
		if (useList.isEmpty() || var.isTypeImmutable()) {
			return assign.getType();
		}
		ArgType type = assign.getType();
		for (RegisterArg arg : useList) {
			ArgType useType = arg.getType();
			ArgType newType = ArgType.merge(dex, type, useType);
			if (newType != null) {
				type = newType;
			}
		}
		return type;
	}

	private static void processPhiNode(PhiInsn phi) {
		ArgType type = phi.getResult().getType();
		if (!type.isTypeKnown()) {
			for (InsnArg arg : phi.getArguments()) {
				if (arg.getType().isTypeKnown()) {
					type = arg.getType();
					break;
				}
			}
		}
		phi.getResult().setType(type);
		for (int i = 0; i < phi.getArgsCount(); i++) {
			RegisterArg arg = phi.getArg(i);
			arg.setType(type);
			SSAVar sVar = arg.getSVar();
			if (sVar != null) {
				sVar.setName(phi.getResult().getName());
			}
		}
	}

	private static String processVarName(SSAVar var) {
		String name = var.getAssign().getName();
		if (name != null) {
			return name;
		}
		for (RegisterArg arg : var.getUseList()) {
			String vName = arg.getName();
			if (vName != null) {
				return vName;
			}
		}
		return null;
	}
}
