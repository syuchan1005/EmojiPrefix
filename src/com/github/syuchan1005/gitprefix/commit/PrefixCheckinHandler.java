package com.github.syuchan1005.gitprefix.commit;

import com.github.syuchan1005.gitprefix.PrefixPanel;
import com.github.syuchan1005.gitprefix.extension.PrefixPanelFactory;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.ui.CommitMessage;
import java.util.Collections;
import javax.swing.AbstractButton;
import javax.swing.JComponent;

/**
 * Created by syuchan on 2017/05/29.
 */
public class PrefixCheckinHandler extends CheckinHandler {
	private static final ExtensionPointName<PrefixPanelFactory> extensionPointName = new ExtensionPointName<>("com.github.syuchan1005.emojiprefix.prefixPanelFactory");

	private PrefixPanel prefixPanel;
	private CheckinProjectPanel checkinProjectPanel;

	private PrefixCheckinHandler(CheckinProjectPanel checkinProjectPanel) {
		this.checkinProjectPanel = checkinProjectPanel;
		prefixPanel = new PrefixPanel(checkinProjectPanel.getProject());
		if (prefixPanel.notExist()) return;
		try {
			Splitter splitter = (Splitter) checkinProjectPanel.getComponent();
			injectPrefixPanel(splitter);
		} catch (Exception ignored) {}
	}

	private void injectPrefixPanel(Splitter splitter) {
		if (prefixPanel.notExist()) return;
		CommitMessage commitMessage = (CommitMessage) splitter.getSecondComponent();
		Splitter commitSplitter = new Splitter();
		commitSplitter.setFirstComponent(prefixPanel);
		commitSplitter.setSecondComponent((JComponent) commitMessage.getComponent(0));
		commitMessage.add(commitSplitter, 0);
		for (PrefixPanelFactory factory : extensionPointName.getExtensions()) {
			factory.createPanel(commitMessage);
		}
	}

	@Override
	public ReturnResult beforeCheckin() {
		if (prefixPanel.notExist()) return ReturnResult.COMMIT;
		for (PrefixPanelFactory prefixPanelFactory: extensionPointName.getExtensions()) {
			if (prefixPanelFactory.beforeCheckin() == PrefixPanelFactory.ReturnResult.CANCEL) {
				return ReturnResult.CANCEL;
			}
		}
		Collections.list(prefixPanel.getButtonGroup().getElements()).stream().filter(AbstractButton::isSelected).findFirst().ifPresent(button -> {
			String prefix = button.getToolTipText();
			if (prefix != null) {
				checkinProjectPanel.setCommitMessage(prefix + " " + checkinProjectPanel.getCommitMessage());
			}
		});
		return ReturnResult.COMMIT;
	}
}
