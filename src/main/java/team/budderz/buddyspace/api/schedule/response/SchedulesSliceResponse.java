package team.budderz.buddyspace.api.schedule.response;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Slice;

public record SchedulesSliceResponse<T>(
	List<T> content,
	boolean hasNext,
	Long nextCursor
) {
	public static <T, ID> SchedulesSliceResponse<T> from(
		Slice<T> slice,
		Function<T, ID> cursorExtractor
	) {
		Long nextCursor = null;

		if (slice.hasNext()) {
			T lastItem = slice.getContent().get(slice.getNumberOfElements() - 1);
			ID cursor = cursorExtractor.apply(lastItem);

			if (cursor instanceof Long) {
				nextCursor = (Long) cursor;
			} else {
				throw new IllegalArgumentException("커서는 Long 타입만 지원됩니다.");
			}
		}

		return new SchedulesSliceResponse<>(
			slice.getContent(),
			slice.hasNext(),
			nextCursor
		);
	}
}
