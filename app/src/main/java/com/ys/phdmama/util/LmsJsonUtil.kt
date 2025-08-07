package com.ys.phdmama.util

import com.ys.phdmama.model.LMSHeadCircumference
import com.ys.phdmama.model.LMSHeightWeight

object LmsJsonUtil {
    const val jsonString = "[\n" +
            "  {\n" +
            "    \"week\": 0,\n" +
            "    \"L\": -0.0631,\n" +
            "    \"M\": 13.3363,\n" +
            "    \"S\": 0.09272,\n" +
            "    \"SD3neg\": 10.1,\n" +
            "    \"SD2neg\": 11.1,\n" +
            "    \"SD1neg\": 12.2,\n" +
            "    \"SD0\": 13.3,\n" +
            "    \"SD1\": 14.6,\n" +
            "    \"SD2\": 16.1,\n" +
            "    \"SD3\": 17.7\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 1,\n" +
            "    \"L\": 0.6319,\n" +
            "    \"M\": 13.2113,\n" +
            "    \"S\": 0.09887,\n" +
            "    \"SD3neg\": 9.5,\n" +
            "    \"SD2neg\": 10.7,\n" +
            "    \"SD1neg\": 11.9,\n" +
            "    \"SD0\": 13.2,\n" +
            "    \"SD1\": 14.5,\n" +
            "    \"SD2\": 15.9,\n" +
            "    \"SD3\": 17.3\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 2,\n" +
            "    \"L\": 0.5082,\n" +
            "    \"M\": 13.4501,\n" +
            "    \"S\": 0.09741,\n" +
            "    \"SD3neg\": 9.8,\n" +
            "    \"SD2neg\": 11,\n" +
            "    \"SD1neg\": 12.2,\n" +
            "    \"SD0\": 13.5,\n" +
            "    \"SD1\": 14.8,\n" +
            "    \"SD2\": 16.2,\n" +
            "    \"SD3\": 17.7\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 3,\n" +
            "    \"L\": 0.4263,\n" +
            "    \"M\": 13.9505,\n" +
            "    \"S\": 0.09647,\n" +
            "    \"SD3neg\": 10.2,\n" +
            "    \"SD2neg\": 11.4,\n" +
            "    \"SD1neg\": 12.6,\n" +
            "    \"SD0\": 14,\n" +
            "    \"SD1\": 15.3,\n" +
            "    \"SD2\": 16.8,\n" +
            "    \"SD3\": 18.3\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 4,\n" +
            "    \"L\": 0.3637,\n" +
            "    \"M\": 14.4208,\n" +
            "    \"S\": 0.09577,\n" +
            "    \"SD3neg\": 10.6,\n" +
            "    \"SD2neg\": 11.8,\n" +
            "    \"SD1neg\": 13.1,\n" +
            "    \"SD0\": 14.4,\n" +
            "    \"SD1\": 15.8,\n" +
            "    \"SD2\": 17.4,\n" +
            "    \"SD3\": 19\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 5,\n" +
            "    \"L\": 0.3124,\n" +
            "    \"M\": 14.8157,\n" +
            "    \"S\": 0.0952,\n" +
            "    \"SD3neg\": 11,\n" +
            "    \"SD2neg\": 12.2,\n" +
            "    \"SD1neg\": 13.5,\n" +
            "    \"SD0\": 14.8,\n" +
            "    \"SD1\": 16.3,\n" +
            "    \"SD2\": 17.8,\n" +
            "    \"SD3\": 19.5\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 6,\n" +
            "    \"L\": 0.2688,\n" +
            "    \"M\": 15.138,\n" +
            "    \"S\": 0.09472,\n" +
            "    \"SD3neg\": 11.3,\n" +
            "    \"SD2neg\": 12.5,\n" +
            "    \"SD1neg\": 13.8,\n" +
            "    \"SD0\": 15.1,\n" +
            "    \"SD1\": 16.6,\n" +
            "    \"SD2\": 18.2,\n" +
            "    \"SD3\": 19.9\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 7,\n" +
            "    \"L\": 0.2306,\n" +
            "    \"M\": 15.4063,\n" +
            "    \"S\": 0.09431,\n" +
            "    \"SD3neg\": 11.5,\n" +
            "    \"SD2neg\": 12.7,\n" +
            "    \"SD1neg\": 14,\n" +
            "    \"SD0\": 15.4,\n" +
            "    \"SD1\": 16.9,\n" +
            "    \"SD2\": 18.5,\n" +
            "    \"SD3\": 20.3\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 8,\n" +
            "    \"L\": 0.1966,\n" +
            "    \"M\": 15.6311,\n" +
            "    \"S\": 0.09394,\n" +
            "    \"SD3neg\": 11.7,\n" +
            "    \"SD2neg\": 12.9,\n" +
            "    \"SD1neg\": 14.2,\n" +
            "    \"SD0\": 15.6,\n" +
            "    \"SD1\": 17.2,\n" +
            "    \"SD2\": 18.8,\n" +
            "    \"SD3\": 20.6\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 9,\n" +
            "    \"L\": 0.1658,\n" +
            "    \"M\": 15.8232,\n" +
            "    \"S\": 0.09361,\n" +
            "    \"SD3neg\": 11.9,\n" +
            "    \"SD2neg\": 13.1,\n" +
            "    \"SD1neg\": 14.4,\n" +
            "    \"SD0\": 15.8,\n" +
            "    \"SD1\": 17.4,\n" +
            "    \"SD2\": 19,\n" +
            "    \"SD3\": 20.8\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 10,\n" +
            "    \"L\": 0.1377,\n" +
            "    \"M\": 15.9874,\n" +
            "    \"S\": 0.09332,\n" +
            "    \"SD3neg\": 12,\n" +
            "    \"SD2neg\": 13.2,\n" +
            "    \"SD1neg\": 14.6,\n" +
            "    \"SD0\": 16,\n" +
            "    \"SD1\": 17.5,\n" +
            "    \"SD2\": 19.2,\n" +
            "    \"SD3\": 21\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 11,\n" +
            "    \"L\": 0.1118,\n" +
            "    \"M\": 16.1277,\n" +
            "    \"S\": 0.09304,\n" +
            "    \"SD3neg\": 12.1,\n" +
            "    \"SD2neg\": 13.4,\n" +
            "    \"SD1neg\": 14.7,\n" +
            "    \"SD0\": 16.1,\n" +
            "    \"SD1\": 17.7,\n" +
            "    \"SD2\": 19.4,\n" +
            "    \"SD3\": 21.2\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 12,\n" +
            "    \"L\": 0.0877,\n" +
            "    \"M\": 16.2485,\n" +
            "    \"S\": 0.09279,\n" +
            "    \"SD3neg\": 12.3,\n" +
            "    \"SD2neg\": 13.5,\n" +
            "    \"SD1neg\": 14.8,\n" +
            "    \"SD0\": 16.2,\n" +
            "    \"SD1\": 17.8,\n" +
            "    \"SD2\": 19.5,\n" +
            "    \"SD3\": 21.4\n" +
            "  },\n" +
            "  {\n" +
            "    \"week\": 13,\n" +
            "    \"L\": 0.0652,\n" +
            "    \"M\": 16.3531,\n" +
            "    \"S\": 0.09255,\n" +
            "    \"SD3neg\": 12.4,\n" +
            "    \"SD2neg\": 13.6,\n" +
            "    \"SD1neg\": 14.9,\n" +
            "    \"SD0\": 16.4,\n" +
            "    \"SD1\": 17.9,\n" +
            "    \"SD2\": 19.7,\n" +
            "    \"SD3\": 21.5\n" +
            "  }\n" +
            "]\n"

    val lmsDataGirls = listOf(
        LMSHeadCircumference(0, "girl", 1.0, 33.8787, 0.03496),
        LMSHeadCircumference(1, "girl", 1.0, 34.5529, 0.03374),
        LMSHeadCircumference(2, "girl", 1.0, 35.2272, 0.03251),
        LMSHeadCircumference(3, "girl", 1.0, 35.843, 0.03231),
        LMSHeadCircumference(4, "girl", 1.0, 36.3761, 0.03215),
        LMSHeadCircumference(5, "girl", 1.0, 36.8472, 0.03202),
        LMSHeadCircumference(6, "girl", 1.0, 37.2711, 0.03191),
        LMSHeadCircumference(7, "girl", 1.0, 37.6584, 0.03182),
        LMSHeadCircumference(8, "girl", 1.0, 38.0167, 0.03173),
        LMSHeadCircumference(9, "girl", 1.0, 38.3516, 0.03166),
        LMSHeadCircumference(10, "girl", 1.0, 38.6673, 0.03158),
        LMSHeadCircumference(11, "girl", 1.0, 38.9661, 0.03152),
        LMSHeadCircumference(12, "girl", 1.0, 39.2501, 0.03146),
        LMSHeadCircumference(13, "girl", 1.0, 39.521, 0.0314)
    )

    val lmsDataHeightWeightGirls = listOf(
        LMSHeightWeight(0, "girl", 1.0, 49.1477, 0.0379),
        LMSHeightWeight(1, "girl", 1.0, 50.3298, 0.03742),
        LMSHeightWeight(2, "girl", 1.0, 51.512, 0.03694),
        LMSHeightWeight(3, "girl", 1.0, 52.4695, 0.03669),
        LMSHeightWeight(4, "girl", 1.0, 53.3809, 0.03647),
        LMSHeightWeight(5, "girl", 1.0, 54.2454, 0.03627),
        LMSHeightWeight(6, "girl", 1.0, 55.0642, 0.03609),
        LMSHeightWeight(7, "girl", 1.0, 55.8406, 0.03593),
        LMSHeightWeight(8, "girl", 1.0, 56.5767, 0.03578),
        LMSHeightWeight(9, "girl", 1.0, 57.2761, 0.03564),
        LMSHeightWeight(10, "girl", 1.0, 57.9436, 0.03552),
        LMSHeightWeight(11, "girl", 1.0, 58.5816, 0.0354),
        LMSHeightWeight(12, "girl", 1.0, 59.1922, 0.0353),
        LMSHeightWeight(13, "girl", 1.0, 59.7773, 0.0352)
    )

}
