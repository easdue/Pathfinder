package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.Manifest;
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
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.FinishState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowMessageState;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;

import static org.junit.Assert.assertNotNull;
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
    public void whenHandleMessageCalled_resultsInShowMessageViewState() {
        MainActivityViewModel viewModel = new MainActivityViewModel();
        LiveData<MainActivityViewState> liveData = viewModel.getMainActivityViewState();
        liveData.observeForever(observer);

        MainActivityViewState prevState = liveData.getValue();

        MessageWithTitle messageWithTitle = new MessageWithTitle(R.string.storage_error, R.string.storage_adopted_storage_unsupported);

        viewModel.handleMessage(messageWithTitle, true);

        MainActivityViewState newState = liveData.getValue();

        assert(newState instanceof ShowMessageState);

        ShowMessageState showMessageState = (ShowMessageState) newState;

        assert(showMessageState.message == messageWithTitle);
        assertTrue(showMessageState.isFatal);
        assert(showMessageState.prevState == prevState);
    }

    @Test
    public void whenOnStorageInitializedIsCalled_resultsInRequestRuntimePermissionState() {
        MainActivityViewModel viewModel = new MainActivityViewModel();
        viewModel.onStorageInitialized();

        assert(viewModel.getMainActivityViewState().getValue() instanceof RequestRuntimePermissionState);
        RequestRuntimePermissionState state = (RequestRuntimePermissionState) viewModel.getMainActivityViewState().getValue();
        assertNotNull(state.request);
        assert(state.request.getPermission().equals(android.Manifest.permission.ACCESS_FINE_LOCATION));
        assertNotNull(state.request.getPermissionRationaleMessage());
    }

    @Test
    public void whenOnMessageDismissedForFatalMessageIsCalled_resultsInPreviousViewState() {
        MainActivityViewModel viewModel = new MainActivityViewModel();
        viewModel.onStorageInitialized();

        MainActivityViewState prevState = viewModel.getMainActivityViewState().getValue();
        viewModel.handleMessage(new MessageWithTitle(1,2), false);
        ShowMessageState showMessageState = (ShowMessageState) viewModel.getMainActivityViewState().getValue();
        viewModel.onMessageDismissed(showMessageState);

        assert(viewModel.getMainActivityViewState().getValue() == prevState);
    }

    @Test
    public void whenOnMessageDismissedForFatalMessageIsCalled_resultsInFinishState() {
        MainActivityViewModel viewModel = new MainActivityViewModel();
        viewModel.onStorageInitialized();

        viewModel.handleMessage(new MessageWithTitle(1, 2), true);
        ShowMessageState showMessageState = (ShowMessageState) viewModel.getMainActivityViewState().getValue();
        viewModel.onMessageDismissed(showMessageState);

        assert(viewModel.getMainActivityViewState().getValue() instanceof FinishState);
    }

    @Test
    public void whenOnPermissionDeniedIsCalled_resultsInFatalShowMessageState() {
        MainActivityViewModel viewModel = new MainActivityViewModel();
        viewModel.onPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION);

        assert(viewModel.getMainActivityViewState().getValue() instanceof ShowMessageState);
        ShowMessageState state = (ShowMessageState) viewModel.getMainActivityViewState().getValue();
        assert(state.message.titleResId == R.string.fatal_error);
        assert(state.message.messageResId == R.string.location_permission_is_required);
        assertTrue(state.isFatal);
    }
}