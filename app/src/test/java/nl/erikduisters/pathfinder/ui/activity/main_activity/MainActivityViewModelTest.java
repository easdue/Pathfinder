package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * Created by Erik Duisters on 08-06-2018.
 */
@RunWith(JUnit4.class)
public class MainActivityViewModelTest {
    @Mock
    private Observer<MainActivityViewState> observer;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void whenCreated_startsWithInitStorageViewState() {
        MainActivityViewModel viewModel = new MainActivityViewModel();
        LiveData<MainActivityViewState> liveData = viewModel.getMainActivityViewState();
        liveData.observeForever(observer);

        verify(observer).onChanged(ArgumentMatchers.isA(MainActivityViewState.InitStorageViewState.class));
    }

    @Test
    public void whenHandleMessageCalled_setsShowMessageViewState() {
        MainActivityViewModel viewModel = new MainActivityViewModel();
        LiveData<MainActivityViewState> liveData = viewModel.getMainActivityViewState();
        liveData.observeForever(observer);

        MainActivityViewState prevState = liveData.getValue();

        MessageWithTitle messageWithTitle = new MessageWithTitle(R.string.storage_error, R.string.storage_adopted_storage_unsupported);

        viewModel.handleMessage(messageWithTitle, true);

        MainActivityViewState newState = liveData.getValue();

        assert(newState instanceof MainActivityViewState.ShowMessageViewState);

        MainActivityViewState.ShowMessageViewState showMessageViewState = (MainActivityViewState.ShowMessageViewState) newState;

        assert(showMessageViewState.message == messageWithTitle);
        assertTrue(showMessageViewState.isFatal);
        assert(showMessageViewState.prevState == prevState);
    }
}