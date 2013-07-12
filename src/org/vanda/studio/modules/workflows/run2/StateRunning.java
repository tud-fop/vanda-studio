package org.vanda.studio.modules.workflows.run2;

import java.io.InputStream;
import java.util.Date;

import org.vanda.execution.model.ExecutableWorkflow;
import org.vanda.execution.model.Runables.RunCancelledAll;
import org.vanda.execution.model.Runables.RunEvent;
import org.vanda.fragment.model.Fragment;
import org.vanda.fragment.model.Fragments;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.run2.Runs.RunState;
import org.vanda.studio.modules.workflows.run2.Runs.RunTransitions;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.RCChecker;

public class StateRunning extends RunState {

	private StreamGobbler esg;
	private StreamGobbler isg;
	private MultiplexObserver<RunEvent> observable;
	private Process process;

	public StateRunning(ExecutableWorkflow ew, Fragment fragment, Application app) {
		ew.registerRunEventListener(observable);
		try {
			process = Runtime.getRuntime().exec(
					RCChecker.getOutPath() + "/"
							+ Fragments.normalize(fragment.getId()), null, null);

		} catch (Exception e) {
			app.sendMessage(new ExceptionMessage(e));
		}

		InputStream stdin = process.getInputStream();
		InputStream stderr = process.getErrorStream();
		isg = new StreamGobbler(stdin, observable);
		esg = new StreamGobbler(stderr, observable);
		isg.start();
		esg.start();
	}
	@Override
	void cancel(RunTransitions rt) {
		process.destroy();
		process = null;
		observable.notify(new RunCancelledAll());
		rt.doCancel();
	}
	@Override
	void finish(RunTransitions rt) {
		int i = 0;
		try {
			i = process.waitFor();
		} catch (Exception e) {
			// ignore
		}
		if (i == 0)
			rt.doFinish();
		else
			rt.doCancel();
	}
	String getString(Date date) {
		return "[Running] " + date.toString();
	}

}
