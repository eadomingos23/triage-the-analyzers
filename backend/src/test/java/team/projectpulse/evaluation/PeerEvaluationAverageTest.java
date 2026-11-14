
package team.projectpulse.evaluation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PeerEvaluationAverageTest {

    @Test
    void changingOriginalCommentsDoesNotChangeReport() {
        PeerEvaluationAverage report = new PeerEvaluationAverage();
        List<String> comments = new ArrayList<>(List.of("Great teamwork"));

        report.setPublicComments(comments);
        comments.add("Unexpected change");

        assertThat(report.getPublicComments())
                .containsExactly("Great teamwork");
    }

    @Test
    void changingReturnedCommentsDoesNotChangeReport() {
        PeerEvaluationAverage report = new PeerEvaluationAverage();
        report.setPrivateComments(
                new ArrayList<>(List.of("Original feedback"))
        );

        List<String> returnedComments = report.getPrivateComments();
        returnedComments.add("Unexpected change");

        assertThat(report.getPrivateComments())
                .containsExactly("Original feedback");
    }
}
