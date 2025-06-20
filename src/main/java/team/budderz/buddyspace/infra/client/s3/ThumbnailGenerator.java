package team.budderz.buddyspace.infra.client.s3;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ThumbnailGenerator {

    public static BufferedImage generate(File videoFile) throws IOException, JCodecException {
        // videoFile 에서 프레임을 추출할 수 있는 FrameGrab 객체 생성
        // NIOUtils.readableChannel(videoFile) = 영상 파일을 읽기 전용으로 여는 스트림
        FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile));
        grab.seekToSecondPrecise(2); // 영상의 2초 지점으로 이동
        Picture picture = grab.getNativeFrame(); // 원시 영상 프레임 객체

        return AWTUtil.toBufferedImage(picture); // 읽어온 프레임을 BufferedImage 로 변환해서 반환
    }
}
