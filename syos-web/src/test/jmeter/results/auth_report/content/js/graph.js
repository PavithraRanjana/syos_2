/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
$(document).ready(function() {

    $(".click-title").mouseenter( function(    e){
        e.preventDefault();
        this.style.cursor="pointer";
    });
    $(".click-title").mousedown( function(event){
        event.preventDefault();
    });

    // Ugly code while this script is shared among several pages
    try{
        refreshHitsPerSecond(true);
    } catch(e){}
    try{
        refreshResponseTimeOverTime(true);
    } catch(e){}
    try{
        refreshResponseTimePercentiles();
    } catch(e){}
});


var responseTimePercentilesInfos = {
        data: {"result": {"minY": 2541.0, "minX": 0.0, "maxY": 10447.0, "series": [{"data": [[0.0, 2541.0], [0.1, 2640.0], [0.2, 2737.0], [0.3, 2737.0], [0.4, 2839.0], [0.5, 2940.0], [0.6, 2979.0], [0.7, 3014.0], [0.8, 3043.0], [0.9, 3139.0], [1.0, 3193.0], [1.1, 3197.0], [1.2, 3210.0], [1.3, 3227.0], [1.4, 3227.0], [1.5, 3240.0], [1.6, 3341.0], [1.7, 3343.0], [1.8, 3429.0], [1.9, 3439.0], [2.0, 3451.0], [2.1, 3466.0], [2.2, 3498.0], [2.3, 3499.0], [2.4, 3511.0], [2.5, 3539.0], [2.6, 3557.0], [2.7, 3590.0], [2.8, 3640.0], [2.9, 3640.0], [3.0, 3722.0], [3.1, 3754.0], [3.2, 3825.0], [3.3, 3877.0], [3.4, 3916.0], [3.5, 3920.0], [3.6, 3970.0], [3.7, 4062.0], [3.8, 4205.0], [3.9, 4233.0], [4.0, 4268.0], [4.1, 4274.0], [4.2, 4308.0], [4.3, 4320.0], [4.4, 4339.0], [4.5, 4379.0], [4.6, 4400.0], [4.7, 4451.0], [4.8, 4467.0], [4.9, 4527.0], [5.0, 4578.0], [5.1, 4655.0], [5.2, 4723.0], [5.3, 4729.0], [5.4, 4773.0], [5.5, 4775.0], [5.6, 4839.0], [5.7, 4963.0], [5.8, 4997.0], [5.9, 5046.0], [6.0, 5078.0], [6.1, 5087.0], [6.2, 5164.0], [6.3, 5164.0], [6.4, 5174.0], [6.5, 5176.0], [6.6, 5271.0], [6.7, 5291.0], [6.8, 5423.0], [6.9, 5426.0], [7.0, 5467.0], [7.1, 5523.0], [7.2, 5540.0], [7.3, 5544.0], [7.4, 5602.0], [7.5, 5695.0], [7.6, 5760.0], [7.7, 5763.0], [7.8, 5766.0], [7.9, 5824.0], [8.0, 5930.0], [8.1, 6046.0], [8.2, 6047.0], [8.3, 6084.0], [8.4, 6134.0], [8.5, 6186.0], [8.6, 6197.0], [8.7, 6252.0], [8.8, 6264.0], [8.9, 6359.0], [9.0, 6432.0], [9.1, 6481.0], [9.2, 6483.0], [9.3, 6487.0], [9.4, 6494.0], [9.5, 6495.0], [9.6, 6544.0], [9.7, 6579.0], [9.8, 6590.0], [9.9, 6591.0], [10.0, 6591.0], [10.1, 6607.0], [10.2, 6611.0], [10.3, 6618.0], [10.4, 6683.0], [10.5, 6695.0], [10.6, 6708.0], [10.7, 6727.0], [10.8, 6753.0], [10.9, 6755.0], [11.0, 6755.0], [11.1, 6768.0], [11.2, 6793.0], [11.3, 6797.0], [11.4, 6845.0], [11.5, 6868.0], [11.6, 6924.0], [11.7, 6958.0], [11.8, 7081.0], [11.9, 7147.0], [12.0, 7183.0], [12.1, 7191.0], [12.2, 7210.0], [12.3, 7247.0], [12.4, 7273.0], [12.5, 7274.0], [12.6, 7278.0], [12.7, 7308.0], [12.8, 7347.0], [12.9, 7350.0], [13.0, 7355.0], [13.1, 7356.0], [13.2, 7357.0], [13.3, 7383.0], [13.4, 7387.0], [13.5, 7398.0], [13.6, 7417.0], [13.7, 7447.0], [13.8, 7475.0], [13.9, 7486.0], [14.0, 7488.0], [14.1, 7488.0], [14.2, 7499.0], [14.3, 7508.0], [14.4, 7531.0], [14.5, 7541.0], [14.6, 7544.0], [14.7, 7553.0], [14.8, 7559.0], [14.9, 7580.0], [15.0, 7604.0], [15.1, 7606.0], [15.2, 7616.0], [15.3, 7632.0], [15.4, 7649.0], [15.5, 7687.0], [15.6, 7746.0], [15.7, 7772.0], [15.8, 7797.0], [15.9, 7816.0], [16.0, 7818.0], [16.1, 7833.0], [16.2, 7846.0], [16.3, 7871.0], [16.4, 7878.0], [16.5, 7882.0], [16.6, 7885.0], [16.7, 7889.0], [16.8, 7895.0], [16.9, 7927.0], [17.0, 8010.0], [17.1, 8028.0], [17.2, 8033.0], [17.3, 8035.0], [17.4, 8054.0], [17.5, 8057.0], [17.6, 8071.0], [17.7, 8081.0], [17.8, 8111.0], [17.9, 8167.0], [18.0, 8245.0], [18.1, 8245.0], [18.2, 8246.0], [18.3, 8254.0], [18.4, 8273.0], [18.5, 8306.0], [18.6, 8319.0], [18.7, 8335.0], [18.8, 8359.0], [18.9, 8368.0], [19.0, 8370.0], [19.1, 8399.0], [19.2, 8399.0], [19.3, 8403.0], [19.4, 8416.0], [19.5, 8420.0], [19.6, 8424.0], [19.7, 8453.0], [19.8, 8493.0], [19.9, 8507.0], [20.0, 8510.0], [20.1, 8531.0], [20.2, 8532.0], [20.3, 8533.0], [20.4, 8544.0], [20.5, 8549.0], [20.6, 8564.0], [20.7, 8569.0], [20.8, 8571.0], [20.9, 8579.0], [21.0, 8588.0], [21.1, 8600.0], [21.2, 8614.0], [21.3, 8615.0], [21.4, 8632.0], [21.5, 8633.0], [21.6, 8667.0], [21.7, 8668.0], [21.8, 8675.0], [21.9, 8675.0], [22.0, 8677.0], [22.1, 8682.0], [22.2, 8685.0], [22.3, 8690.0], [22.4, 8693.0], [22.5, 8696.0], [22.6, 8703.0], [22.7, 8713.0], [22.8, 8720.0], [22.9, 8721.0], [23.0, 8721.0], [23.1, 8726.0], [23.2, 8731.0], [23.3, 8756.0], [23.4, 8757.0], [23.5, 8759.0], [23.6, 8760.0], [23.7, 8768.0], [23.8, 8768.0], [23.9, 8773.0], [24.0, 8775.0], [24.1, 8775.0], [24.2, 8779.0], [24.3, 8779.0], [24.4, 8784.0], [24.5, 8800.0], [24.6, 8812.0], [24.7, 8818.0], [24.8, 8819.0], [24.9, 8820.0], [25.0, 8821.0], [25.1, 8821.0], [25.2, 8822.0], [25.3, 8823.0], [25.4, 8824.0], [25.5, 8825.0], [25.6, 8828.0], [25.7, 8830.0], [25.8, 8832.0], [25.9, 8832.0], [26.0, 8835.0], [26.1, 8838.0], [26.2, 8841.0], [26.3, 8843.0], [26.4, 8844.0], [26.5, 8850.0], [26.6, 8851.0], [26.7, 8854.0], [26.8, 8854.0], [26.9, 8854.0], [27.0, 8862.0], [27.1, 8865.0], [27.2, 8870.0], [27.3, 8870.0], [27.4, 8871.0], [27.5, 8871.0], [27.6, 8871.0], [27.7, 8871.0], [27.8, 8872.0], [27.9, 8873.0], [28.0, 8874.0], [28.1, 8877.0], [28.2, 8879.0], [28.3, 8879.0], [28.4, 8880.0], [28.5, 8880.0], [28.6, 8882.0], [28.7, 8882.0], [28.8, 8883.0], [28.9, 8884.0], [29.0, 8888.0], [29.1, 8888.0], [29.2, 8890.0], [29.3, 8892.0], [29.4, 8892.0], [29.5, 8893.0], [29.6, 8895.0], [29.7, 8895.0], [29.8, 8896.0], [29.9, 8897.0], [30.0, 8897.0], [30.1, 8898.0], [30.2, 8900.0], [30.3, 8904.0], [30.4, 8904.0], [30.5, 8907.0], [30.6, 8911.0], [30.7, 8911.0], [30.8, 8912.0], [30.9, 8915.0], [31.0, 8920.0], [31.1, 8920.0], [31.2, 8920.0], [31.3, 8920.0], [31.4, 8921.0], [31.5, 8923.0], [31.6, 8926.0], [31.7, 8929.0], [31.8, 8930.0], [31.9, 8930.0], [32.0, 8931.0], [32.1, 8931.0], [32.2, 8933.0], [32.3, 8936.0], [32.4, 8937.0], [32.5, 8938.0], [32.6, 8938.0], [32.7, 8939.0], [32.8, 8942.0], [32.9, 8943.0], [33.0, 8943.0], [33.1, 8944.0], [33.2, 8944.0], [33.3, 8945.0], [33.4, 8947.0], [33.5, 8950.0], [33.6, 8951.0], [33.7, 8952.0], [33.8, 8953.0], [33.9, 8954.0], [34.0, 8955.0], [34.1, 8956.0], [34.2, 8958.0], [34.3, 8959.0], [34.4, 8961.0], [34.5, 8963.0], [34.6, 8963.0], [34.7, 8966.0], [34.8, 8966.0], [34.9, 8967.0], [35.0, 8967.0], [35.1, 8968.0], [35.2, 8969.0], [35.3, 8969.0], [35.4, 8970.0], [35.5, 8971.0], [35.6, 8972.0], [35.7, 8972.0], [35.8, 8973.0], [35.9, 8973.0], [36.0, 8973.0], [36.1, 8977.0], [36.2, 8977.0], [36.3, 8979.0], [36.4, 8983.0], [36.5, 8983.0], [36.6, 8985.0], [36.7, 8986.0], [36.8, 8986.0], [36.9, 8987.0], [37.0, 8987.0], [37.1, 8992.0], [37.2, 8993.0], [37.3, 8998.0], [37.4, 8999.0], [37.5, 9000.0], [37.6, 9001.0], [37.7, 9001.0], [37.8, 9003.0], [37.9, 9004.0], [38.0, 9006.0], [38.1, 9006.0], [38.2, 9008.0], [38.3, 9008.0], [38.4, 9009.0], [38.5, 9009.0], [38.6, 9009.0], [38.7, 9010.0], [38.8, 9012.0], [38.9, 9012.0], [39.0, 9012.0], [39.1, 9015.0], [39.2, 9017.0], [39.3, 9017.0], [39.4, 9018.0], [39.5, 9018.0], [39.6, 9018.0], [39.7, 9021.0], [39.8, 9022.0], [39.9, 9024.0], [40.0, 9025.0], [40.1, 9025.0], [40.2, 9027.0], [40.3, 9027.0], [40.4, 9028.0], [40.5, 9031.0], [40.6, 9032.0], [40.7, 9033.0], [40.8, 9033.0], [40.9, 9034.0], [41.0, 9035.0], [41.1, 9036.0], [41.2, 9036.0], [41.3, 9036.0], [41.4, 9039.0], [41.5, 9039.0], [41.6, 9040.0], [41.7, 9040.0], [41.8, 9041.0], [41.9, 9042.0], [42.0, 9043.0], [42.1, 9044.0], [42.2, 9044.0], [42.3, 9044.0], [42.4, 9045.0], [42.5, 9047.0], [42.6, 9048.0], [42.7, 9050.0], [42.8, 9050.0], [42.9, 9051.0], [43.0, 9051.0], [43.1, 9052.0], [43.2, 9053.0], [43.3, 9054.0], [43.4, 9057.0], [43.5, 9058.0], [43.6, 9061.0], [43.7, 9061.0], [43.8, 9063.0], [43.9, 9064.0], [44.0, 9065.0], [44.1, 9066.0], [44.2, 9067.0], [44.3, 9067.0], [44.4, 9068.0], [44.5, 9069.0], [44.6, 9070.0], [44.7, 9070.0], [44.8, 9070.0], [44.9, 9072.0], [45.0, 9072.0], [45.1, 9073.0], [45.2, 9073.0], [45.3, 9075.0], [45.4, 9075.0], [45.5, 9077.0], [45.6, 9078.0], [45.7, 9080.0], [45.8, 9081.0], [45.9, 9083.0], [46.0, 9087.0], [46.1, 9087.0], [46.2, 9088.0], [46.3, 9089.0], [46.4, 9090.0], [46.5, 9091.0], [46.6, 9093.0], [46.7, 9096.0], [46.8, 9098.0], [46.9, 9100.0], [47.0, 9100.0], [47.1, 9103.0], [47.2, 9104.0], [47.3, 9104.0], [47.4, 9104.0], [47.5, 9105.0], [47.6, 9105.0], [47.7, 9106.0], [47.8, 9106.0], [47.9, 9106.0], [48.0, 9106.0], [48.1, 9106.0], [48.2, 9107.0], [48.3, 9112.0], [48.4, 9114.0], [48.5, 9115.0], [48.6, 9116.0], [48.7, 9116.0], [48.8, 9116.0], [48.9, 9117.0], [49.0, 9118.0], [49.1, 9120.0], [49.2, 9125.0], [49.3, 9126.0], [49.4, 9127.0], [49.5, 9128.0], [49.6, 9129.0], [49.7, 9129.0], [49.8, 9130.0], [49.9, 9131.0], [50.0, 9132.0], [50.1, 9133.0], [50.2, 9134.0], [50.3, 9135.0], [50.4, 9136.0], [50.5, 9136.0], [50.6, 9139.0], [50.7, 9140.0], [50.8, 9140.0], [50.9, 9141.0], [51.0, 9141.0], [51.1, 9142.0], [51.2, 9142.0], [51.3, 9143.0], [51.4, 9147.0], [51.5, 9148.0], [51.6, 9149.0], [51.7, 9151.0], [51.8, 9151.0], [51.9, 9151.0], [52.0, 9151.0], [52.1, 9153.0], [52.2, 9155.0], [52.3, 9156.0], [52.4, 9156.0], [52.5, 9157.0], [52.6, 9160.0], [52.7, 9161.0], [52.8, 9164.0], [52.9, 9164.0], [53.0, 9165.0], [53.1, 9169.0], [53.2, 9169.0], [53.3, 9169.0], [53.4, 9170.0], [53.5, 9170.0], [53.6, 9170.0], [53.7, 9174.0], [53.8, 9174.0], [53.9, 9174.0], [54.0, 9174.0], [54.1, 9174.0], [54.2, 9175.0], [54.3, 9176.0], [54.4, 9178.0], [54.5, 9179.0], [54.6, 9179.0], [54.7, 9181.0], [54.8, 9182.0], [54.9, 9182.0], [55.0, 9182.0], [55.1, 9184.0], [55.2, 9184.0], [55.3, 9185.0], [55.4, 9185.0], [55.5, 9186.0], [55.6, 9189.0], [55.7, 9191.0], [55.8, 9191.0], [55.9, 9192.0], [56.0, 9195.0], [56.1, 9197.0], [56.2, 9198.0], [56.3, 9198.0], [56.4, 9199.0], [56.5, 9200.0], [56.6, 9200.0], [56.7, 9201.0], [56.8, 9202.0], [56.9, 9203.0], [57.0, 9203.0], [57.1, 9203.0], [57.2, 9204.0], [57.3, 9205.0], [57.4, 9205.0], [57.5, 9206.0], [57.6, 9206.0], [57.7, 9206.0], [57.8, 9207.0], [57.9, 9215.0], [58.0, 9216.0], [58.1, 9216.0], [58.2, 9216.0], [58.3, 9216.0], [58.4, 9217.0], [58.5, 9218.0], [58.6, 9218.0], [58.7, 9219.0], [58.8, 9221.0], [58.9, 9221.0], [59.0, 9223.0], [59.1, 9227.0], [59.2, 9228.0], [59.3, 9228.0], [59.4, 9229.0], [59.5, 9230.0], [59.6, 9231.0], [59.7, 9231.0], [59.8, 9232.0], [59.9, 9233.0], [60.0, 9234.0], [60.1, 9234.0], [60.2, 9234.0], [60.3, 9235.0], [60.4, 9235.0], [60.5, 9236.0], [60.6, 9238.0], [60.7, 9240.0], [60.8, 9242.0], [60.9, 9242.0], [61.0, 9242.0], [61.1, 9245.0], [61.2, 9248.0], [61.3, 9250.0], [61.4, 9251.0], [61.5, 9253.0], [61.6, 9255.0], [61.7, 9256.0], [61.8, 9256.0], [61.9, 9258.0], [62.0, 9258.0], [62.1, 9259.0], [62.2, 9260.0], [62.3, 9260.0], [62.4, 9261.0], [62.5, 9261.0], [62.6, 9261.0], [62.7, 9262.0], [62.8, 9262.0], [62.9, 9263.0], [63.0, 9264.0], [63.1, 9265.0], [63.2, 9266.0], [63.3, 9266.0], [63.4, 9267.0], [63.5, 9268.0], [63.6, 9270.0], [63.7, 9270.0], [63.8, 9272.0], [63.9, 9273.0], [64.0, 9274.0], [64.1, 9274.0], [64.2, 9277.0], [64.3, 9279.0], [64.4, 9281.0], [64.5, 9286.0], [64.6, 9288.0], [64.7, 9288.0], [64.8, 9289.0], [64.9, 9289.0], [65.0, 9290.0], [65.1, 9291.0], [65.2, 9292.0], [65.3, 9295.0], [65.4, 9296.0], [65.5, 9298.0], [65.6, 9298.0], [65.7, 9300.0], [65.8, 9300.0], [65.9, 9301.0], [66.0, 9302.0], [66.1, 9304.0], [66.2, 9306.0], [66.3, 9306.0], [66.4, 9306.0], [66.5, 9307.0], [66.6, 9308.0], [66.7, 9309.0], [66.8, 9312.0], [66.9, 9313.0], [67.0, 9313.0], [67.1, 9315.0], [67.2, 9316.0], [67.3, 9316.0], [67.4, 9318.0], [67.5, 9320.0], [67.6, 9321.0], [67.7, 9322.0], [67.8, 9322.0], [67.9, 9323.0], [68.0, 9324.0], [68.1, 9324.0], [68.2, 9325.0], [68.3, 9325.0], [68.4, 9325.0], [68.5, 9327.0], [68.6, 9330.0], [68.7, 9333.0], [68.8, 9335.0], [68.9, 9335.0], [69.0, 9338.0], [69.1, 9345.0], [69.2, 9347.0], [69.3, 9348.0], [69.4, 9354.0], [69.5, 9355.0], [69.6, 9359.0], [69.7, 9359.0], [69.8, 9359.0], [69.9, 9359.0], [70.0, 9360.0], [70.1, 9360.0], [70.2, 9361.0], [70.3, 9362.0], [70.4, 9364.0], [70.5, 9367.0], [70.6, 9367.0], [70.7, 9368.0], [70.8, 9369.0], [70.9, 9371.0], [71.0, 9371.0], [71.1, 9373.0], [71.2, 9374.0], [71.3, 9374.0], [71.4, 9377.0], [71.5, 9381.0], [71.6, 9382.0], [71.7, 9382.0], [71.8, 9383.0], [71.9, 9384.0], [72.0, 9384.0], [72.1, 9384.0], [72.2, 9387.0], [72.3, 9387.0], [72.4, 9393.0], [72.5, 9395.0], [72.6, 9396.0], [72.7, 9397.0], [72.8, 9399.0], [72.9, 9400.0], [73.0, 9402.0], [73.1, 9404.0], [73.2, 9406.0], [73.3, 9407.0], [73.4, 9409.0], [73.5, 9414.0], [73.6, 9414.0], [73.7, 9420.0], [73.8, 9421.0], [73.9, 9423.0], [74.0, 9423.0], [74.1, 9424.0], [74.2, 9427.0], [74.3, 9431.0], [74.4, 9434.0], [74.5, 9434.0], [74.6, 9435.0], [74.7, 9436.0], [74.8, 9444.0], [74.9, 9444.0], [75.0, 9445.0], [75.1, 9446.0], [75.2, 9446.0], [75.3, 9451.0], [75.4, 9452.0], [75.5, 9454.0], [75.6, 9456.0], [75.7, 9459.0], [75.8, 9461.0], [75.9, 9469.0], [76.0, 9471.0], [76.1, 9472.0], [76.2, 9473.0], [76.3, 9474.0], [76.4, 9474.0], [76.5, 9475.0], [76.6, 9484.0], [76.7, 9487.0], [76.8, 9492.0], [76.9, 9503.0], [77.0, 9504.0], [77.1, 9505.0], [77.2, 9516.0], [77.3, 9519.0], [77.4, 9521.0], [77.5, 9522.0], [77.6, 9526.0], [77.7, 9539.0], [77.8, 9541.0], [77.9, 9544.0], [78.0, 9555.0], [78.1, 9555.0], [78.2, 9556.0], [78.3, 9560.0], [78.4, 9561.0], [78.5, 9563.0], [78.6, 9563.0], [78.7, 9570.0], [78.8, 9571.0], [78.9, 9579.0], [79.0, 9582.0], [79.1, 9587.0], [79.2, 9587.0], [79.3, 9588.0], [79.4, 9592.0], [79.5, 9594.0], [79.6, 9596.0], [79.7, 9599.0], [79.8, 9600.0], [79.9, 9604.0], [80.0, 9607.0], [80.1, 9617.0], [80.2, 9622.0], [80.3, 9632.0], [80.4, 9636.0], [80.5, 9644.0], [80.6, 9650.0], [80.7, 9666.0], [80.8, 9667.0], [80.9, 9670.0], [81.0, 9671.0], [81.1, 9674.0], [81.2, 9690.0], [81.3, 9694.0], [81.4, 9694.0], [81.5, 9697.0], [81.6, 9700.0], [81.7, 9709.0], [81.8, 9711.0], [81.9, 9717.0], [82.0, 9723.0], [82.1, 9727.0], [82.2, 9734.0], [82.3, 9747.0], [82.4, 9747.0], [82.5, 9750.0], [82.6, 9751.0], [82.7, 9760.0], [82.8, 9765.0], [82.9, 9772.0], [83.0, 9782.0], [83.1, 9789.0], [83.2, 9791.0], [83.3, 9792.0], [83.4, 9797.0], [83.5, 9803.0], [83.6, 9803.0], [83.7, 9805.0], [83.8, 9813.0], [83.9, 9819.0], [84.0, 9826.0], [84.1, 9828.0], [84.2, 9832.0], [84.3, 9833.0], [84.4, 9836.0], [84.5, 9845.0], [84.6, 9845.0], [84.7, 9856.0], [84.8, 9857.0], [84.9, 9858.0], [85.0, 9863.0], [85.1, 9866.0], [85.2, 9867.0], [85.3, 9873.0], [85.4, 9875.0], [85.5, 9877.0], [85.6, 9879.0], [85.7, 9881.0], [85.8, 9881.0], [85.9, 9882.0], [86.0, 9886.0], [86.1, 9886.0], [86.2, 9886.0], [86.3, 9887.0], [86.4, 9889.0], [86.5, 9892.0], [86.6, 9897.0], [86.7, 9897.0], [86.8, 9898.0], [86.9, 9898.0], [87.0, 9899.0], [87.1, 9900.0], [87.2, 9910.0], [87.3, 9917.0], [87.4, 9919.0], [87.5, 9921.0], [87.6, 9924.0], [87.7, 9925.0], [87.8, 9935.0], [87.9, 9940.0], [88.0, 9942.0], [88.1, 9946.0], [88.2, 9973.0], [88.3, 9977.0], [88.4, 9979.0], [88.5, 9981.0], [88.6, 9985.0], [88.7, 9989.0], [88.8, 9989.0], [88.9, 9989.0], [89.0, 9993.0], [89.1, 9999.0], [89.2, 10002.0], [89.3, 10005.0], [89.4, 10009.0], [89.5, 10011.0], [89.6, 10020.0], [89.7, 10022.0], [89.8, 10023.0], [89.9, 10024.0], [90.0, 10025.0], [90.1, 10028.0], [90.2, 10030.0], [90.3, 10031.0], [90.4, 10032.0], [90.5, 10034.0], [90.6, 10036.0], [90.7, 10038.0], [90.8, 10042.0], [90.9, 10043.0], [91.0, 10044.0], [91.1, 10047.0], [91.2, 10049.0], [91.3, 10052.0], [91.4, 10054.0], [91.5, 10055.0], [91.6, 10056.0], [91.7, 10058.0], [91.8, 10061.0], [91.9, 10061.0], [92.0, 10062.0], [92.1, 10062.0], [92.2, 10063.0], [92.3, 10064.0], [92.4, 10077.0], [92.5, 10077.0], [92.6, 10077.0], [92.7, 10077.0], [92.8, 10086.0], [92.9, 10086.0], [93.0, 10087.0], [93.1, 10094.0], [93.2, 10096.0], [93.3, 10097.0], [93.4, 10100.0], [93.5, 10106.0], [93.6, 10108.0], [93.7, 10109.0], [93.8, 10112.0], [93.9, 10114.0], [94.0, 10119.0], [94.1, 10120.0], [94.2, 10122.0], [94.3, 10123.0], [94.4, 10129.0], [94.5, 10131.0], [94.6, 10133.0], [94.7, 10135.0], [94.8, 10144.0], [94.9, 10145.0], [95.0, 10151.0], [95.1, 10154.0], [95.2, 10155.0], [95.3, 10157.0], [95.4, 10158.0], [95.5, 10160.0], [95.6, 10162.0], [95.7, 10165.0], [95.8, 10167.0], [95.9, 10183.0], [96.0, 10187.0], [96.1, 10189.0], [96.2, 10190.0], [96.3, 10194.0], [96.4, 10195.0], [96.5, 10196.0], [96.6, 10196.0], [96.7, 10199.0], [96.8, 10208.0], [96.9, 10211.0], [97.0, 10213.0], [97.1, 10230.0], [97.2, 10237.0], [97.3, 10239.0], [97.4, 10239.0], [97.5, 10243.0], [97.6, 10244.0], [97.7, 10250.0], [97.8, 10257.0], [97.9, 10259.0], [98.0, 10282.0], [98.1, 10286.0], [98.2, 10292.0], [98.3, 10292.0], [98.4, 10307.0], [98.5, 10328.0], [98.6, 10334.0], [98.7, 10336.0], [98.8, 10364.0], [98.9, 10373.0], [99.0, 10382.0], [99.1, 10403.0], [99.2, 10407.0], [99.3, 10409.0], [99.4, 10410.0], [99.5, 10411.0], [99.6, 10420.0], [99.7, 10445.0], [99.8, 10446.0], [99.9, 10447.0]], "isOverall": false, "label": "Login Request", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 100.0, "title": "Response Time Percentiles"}},
        getOptions: function() {
            return {
                series: {
                    points: { show: false }
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimePercentiles'
                },
                xaxis: {
                    tickDecimals: 1,
                    axisLabel: "Percentiles",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Percentile value in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : %x.2 percentile was %y ms"
                },
                selection: { mode: "xy" },
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimePercentiles"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesPercentiles"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesPercentiles"), dataset, prepareOverviewOptions(options));
        }
};

/**
 * @param elementId Id of element where we display message
 */
function setEmptyGraph(elementId) {
    $(function() {
        $(elementId).text("No graph series with filter="+seriesFilter);
    });
}

// Response times percentiles
function refreshResponseTimePercentiles() {
    var infos = responseTimePercentilesInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimePercentiles");
        return;
    }
    if (isGraph($("#flotResponseTimesPercentiles"))){
        infos.createGraph();
    } else {
        var choiceContainer = $("#choicesResponseTimePercentiles");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesPercentiles", "#overviewResponseTimesPercentiles");
        $('#bodyResponseTimePercentiles .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimeDistributionInfos = {
        data: {"result": {"minY": 1.0, "minX": 2500.0, "maxY": 96.0, "series": [{"data": [[2500.0, 1.0], [2600.0, 1.0], [2700.0, 2.0], [2800.0, 1.0], [2900.0, 2.0], [3000.0, 2.0], [3100.0, 3.0], [3300.0, 2.0], [3200.0, 3.0], [3400.0, 6.0], [3500.0, 4.0], [3600.0, 2.0], [3700.0, 2.0], [3800.0, 2.0], [3900.0, 3.0], [4000.0, 1.0], [4200.0, 4.0], [4300.0, 5.0], [4500.0, 2.0], [4600.0, 1.0], [4400.0, 3.0], [4700.0, 4.0], [4800.0, 1.0], [4900.0, 2.0], [5100.0, 4.0], [5000.0, 3.0], [5200.0, 2.0], [5500.0, 3.0], [5600.0, 2.0], [5400.0, 3.0], [5800.0, 1.0], [5700.0, 3.0], [6000.0, 3.0], [6100.0, 3.0], [5900.0, 1.0], [6200.0, 2.0], [6300.0, 1.0], [6400.0, 6.0], [6500.0, 5.0], [6600.0, 5.0], [6700.0, 8.0], [6800.0, 2.0], [6900.0, 2.0], [7000.0, 1.0], [7100.0, 3.0], [7200.0, 5.0], [7300.0, 9.0], [7400.0, 7.0], [7600.0, 6.0], [7500.0, 7.0], [7800.0, 10.0], [7700.0, 3.0], [7900.0, 1.0], [8000.0, 8.0], [8100.0, 2.0], [8300.0, 7.0], [8400.0, 6.0], [8200.0, 5.0], [8600.0, 15.0], [8500.0, 12.0], [8700.0, 19.0], [8800.0, 57.0], [9200.0, 92.0], [8900.0, 73.0], [9000.0, 94.0], [9100.0, 96.0], [9400.0, 40.0], [9500.0, 30.0], [9300.0, 72.0], [9600.0, 18.0], [9700.0, 19.0], [9800.0, 36.0], [9900.0, 21.0], [10000.0, 42.0], [10100.0, 34.0], [10200.0, 16.0], [10400.0, 9.0], [10300.0, 7.0]], "isOverall": false, "label": "Login Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 100, "maxX": 10400.0, "title": "Response Time Distribution"}},
        getOptions: function() {
            var granularity = this.data.result.granularity;
            return {
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimeDistribution'
                },
                xaxis:{
                    axisLabel: "Response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                bars : {
                    show: true,
                    barWidth: this.data.result.granularity
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: function(label, xval, yval, flotItem){
                        return yval + " responses for " + label + " were between " + xval + " and " + (xval + granularity) + " ms";
                    }
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimeDistribution"), prepareData(data.result.series, $("#choicesResponseTimeDistribution")), options);
        }

};

// Response time distribution
function refreshResponseTimeDistribution() {
    var infos = responseTimeDistributionInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimeDistribution");
        return;
    }
    if (isGraph($("#flotResponseTimeDistribution"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimeDistribution");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        $('#footerResponseTimeDistribution .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var syntheticResponseTimeDistributionInfos = {
        data: {"result": {"minY": 1000.0, "minX": 2.0, "ticks": [[0, "Requests having \nresponse time <= 500ms"], [1, "Requests having \nresponse time > 500ms and <= 1,500ms"], [2, "Requests having \nresponse time > 1,500ms"], [3, "Requests in error"]], "maxY": 1000.0, "series": [{"data": [], "color": "#9ACD32", "isOverall": false, "label": "Requests having \nresponse time <= 500ms", "isController": false}, {"data": [], "color": "yellow", "isOverall": false, "label": "Requests having \nresponse time > 500ms and <= 1,500ms", "isController": false}, {"data": [[2.0, 1000.0]], "color": "orange", "isOverall": false, "label": "Requests having \nresponse time > 1,500ms", "isController": false}, {"data": [], "color": "#FF6347", "isOverall": false, "label": "Requests in error", "isController": false}], "supportsControllersDiscrimination": false, "maxX": 2.0, "title": "Synthetic Response Times Distribution"}},
        getOptions: function() {
            return {
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendSyntheticResponseTimeDistribution'
                },
                xaxis:{
                    axisLabel: "Response times ranges",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                    tickLength:0,
                    min:-0.5,
                    max:3.5
                },
                yaxis: {
                    axisLabel: "Number of responses",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                bars : {
                    show: true,
                    align: "center",
                    barWidth: 0.25,
                    fill:.75
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: function(label, xval, yval, flotItem){
                        return yval + " " + label;
                    }
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var options = this.getOptions();
            prepareOptions(options, data);
            options.xaxis.ticks = data.result.ticks;
            $.plot($("#flotSyntheticResponseTimeDistribution"), prepareData(data.result.series, $("#choicesSyntheticResponseTimeDistribution")), options);
        }

};

// Response time distribution
function refreshSyntheticResponseTimeDistribution() {
    var infos = syntheticResponseTimeDistributionInfos;
    prepareSeries(infos.data, true);
    if (isGraph($("#flotSyntheticResponseTimeDistribution"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesSyntheticResponseTimeDistribution");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        $('#footerSyntheticResponseTimeDistribution .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var activeThreadsOverTimeInfos = {
        data: {"result": {"minY": 71.68722466960348, "minX": 1.7706273E12, "maxY": 100.0, "series": [{"data": [[1.77062736E12, 100.0], [1.77062742E12, 71.68722466960348], [1.7706273E12, 88.78102189781018]], "isOverall": false, "label": "Auth Users", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77062742E12, "title": "Active Threads Over Time"}},
        getOptions: function() {
            return {
                series: {
                    stack: true,
                    lines: {
                        show: true,
                        fill: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 6,
                    show: true,
                    container: '#legendActiveThreadsOverTime'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                selection: {
                    mode: 'xy'
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : At %x there were %y active threads"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesActiveThreadsOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotActiveThreadsOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewActiveThreadsOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Active Threads Over Time
function refreshActiveThreadsOverTime(fixTimestamps) {
    var infos = activeThreadsOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 19800000);
    }
    if(isGraph($("#flotActiveThreadsOverTime"))) {
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesActiveThreadsOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotActiveThreadsOverTime", "#overviewActiveThreadsOverTime");
        $('#footerActiveThreadsOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var timeVsThreadsInfos = {
        data: {"result": {"minY": 3014.0, "minX": 1.0, "maxY": 9685.5, "series": [{"data": [[2.0, 3014.0], [3.0, 3227.0], [4.0, 3429.0], [5.0, 3343.0], [6.0, 3193.0], [7.0, 3466.0], [8.0, 3722.0], [9.0, 3557.0], [10.0, 3590.0], [11.0, 3877.0], [12.0, 4062.0], [13.0, 3825.0], [14.0, 3920.0], [15.0, 3916.0], [16.0, 4379.0], [17.0, 4467.0], [18.0, 4338.0], [19.0, 4308.0], [20.0, 4320.0], [21.0, 4205.0], [22.0, 4451.0], [23.0, 4400.0], [24.0, 4723.0], [25.0, 4729.0], [26.0, 4775.0], [27.0, 4527.0], [28.0, 5176.0], [29.0, 4655.0], [31.0, 5121.0], [33.0, 5271.0], [32.0, 4963.0], [35.0, 5046.0], [34.0, 4839.0], [37.0, 5423.0], [36.0, 5164.0], [38.0, 3271.8571428571427], [39.0, 5467.0], [41.0, 4081.5], [40.0, 5602.0], [43.0, 5760.0], [42.0, 5544.0], [44.0, 4454.5], [45.0, 5766.0], [47.0, 6046.0], [46.0, 6084.0], [48.0, 4672.0], [49.0, 6264.0], [51.0, 6186.0], [50.0, 6359.0], [52.0, 4476.666666666667], [53.0, 6483.0], [54.0, 5027.5], [55.0, 4965.5], [57.0, 6755.0], [56.0, 6755.0], [58.0, 5124.0], [59.0, 6845.0], [61.0, 5383.5], [60.0, 6683.0], [63.0, 7388.0], [62.0, 7263.666666666667], [65.0, 5827.5], [66.0, 6335.333333333333], [67.0, 6826.8], [64.0, 7340.333333333333], [69.0, 5868.5], [71.0, 7609.5], [70.0, 7391.666666666667], [68.0, 7488.0], [72.0, 7539.357142857142], [75.0, 6422.0], [74.0, 7882.0], [73.0, 8173.5], [78.0, 7613.25], [79.0, 8496.333333333334], [77.0, 8370.0], [76.0, 8300.363636363636], [82.0, 7868.75], [83.0, 8895.0], [81.0, 8509.5], [80.0, 8703.0], [87.0, 8015.333333333333], [86.0, 9338.5], [85.0, 9026.875], [84.0, 8801.5], [89.0, 7520.0], [91.0, 8150.333333333333], [90.0, 9251.0], [88.0, 9588.0], [92.0, 7047.333333333333], [95.0, 9561.0], [94.0, 9526.0], [93.0, 9423.0], [97.0, 7735.5], [99.0, 9516.0], [98.0, 9471.0], [96.0, 9685.5], [100.0, 9245.808219178081], [1.0, 3197.0]], "isOverall": false, "label": "Login Request", "isController": false}, {"data": [[92.0359999999999, 8703.038]], "isOverall": false, "label": "Login Request-Aggregated", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 100.0, "title": "Time VS Threads"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: { noColumns: 2,show: true, container: '#legendTimeVsThreads' },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s: At %x.2 active threads, Average response time was %y.2 ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesTimeVsThreads"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotTimesVsThreads"), dataset, options);
            // setup overview
            $.plot($("#overviewTimesVsThreads"), dataset, prepareOverviewOptions(options));
        }
};

// Time vs threads
function refreshTimeVsThreads(){
    var infos = timeVsThreadsInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyTimeVsThreads");
        return;
    }
    if(isGraph($("#flotTimesVsThreads"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTimeVsThreads");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTimesVsThreads", "#overviewTimesVsThreads");
        $('#footerTimeVsThreads .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var bytesThroughputOverTimeInfos = {
        data : {"result": {"minY": 573.1166666666667, "minX": 1.7706273E12, "maxY": 8839.25, "series": [{"data": [[1.77062736E12, 8839.25], [1.77062742E12, 3154.8333333333335], [1.7706273E12, 1904.0166666666667]], "isOverall": false, "label": "Bytes received per second", "isController": false}, {"data": [[1.77062736E12, 2660.6], [1.77062742E12, 949.6166666666667], [1.7706273E12, 573.1166666666667]], "isOverall": false, "label": "Bytes sent per second", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77062742E12, "title": "Bytes Throughput Over Time"}},
        getOptions : function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity) ,
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Bytes / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendBytesThroughputOverTime'
                },
                selection: {
                    mode: "xy"
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y"
                }
            };
        },
        createGraph : function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesBytesThroughputOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotBytesThroughputOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewBytesThroughputOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Bytes throughput Over Time
function refreshBytesThroughputOverTime(fixTimestamps) {
    var infos = bytesThroughputOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 19800000);
    }
    if(isGraph($("#flotBytesThroughputOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesBytesThroughputOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotBytesThroughputOverTime", "#overviewBytesThroughputOverTime");
        $('#footerBytesThroughputOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimesOverTimeInfos = {
        data: {"result": {"minY": 7081.416058394157, "minX": 1.7706273E12, "maxY": 9328.322327044036, "series": [{"data": [[1.77062736E12, 9328.322327044036], [1.77062742E12, 7929.828193832607], [1.7706273E12, 7081.416058394157]], "isOverall": false, "label": "Login Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77062742E12, "title": "Response Time Over Time"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average response time was %y ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Response Times Over Time
function refreshResponseTimeOverTime(fixTimestamps) {
    var infos = responseTimesOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimeOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 19800000);
    }
    if(isGraph($("#flotResponseTimesOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesOverTime", "#overviewResponseTimesOverTime");
        $('#footerResponseTimesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var latenciesOverTimeInfos = {
        data: {"result": {"minY": 7075.328467153286, "minX": 1.7706273E12, "maxY": 9324.20440251573, "series": [{"data": [[1.77062736E12, 9324.20440251573], [1.77062742E12, 7928.06607929515], [1.7706273E12, 7075.328467153286]], "isOverall": false, "label": "Login Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77062742E12, "title": "Latencies Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response latencies in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendLatenciesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average latency was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesLatenciesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotLatenciesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewLatenciesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Latencies Over Time
function refreshLatenciesOverTime(fixTimestamps) {
    var infos = latenciesOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyLatenciesOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 19800000);
    }
    if(isGraph($("#flotLatenciesOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesLatenciesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotLatenciesOverTime", "#overviewLatenciesOverTime");
        $('#footerLatenciesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var connectTimeOverTimeInfos = {
        data: {"result": {"minY": 0.09276729559748424, "minX": 1.7706273E12, "maxY": 1.1240875912408759, "series": [{"data": [[1.77062736E12, 0.09276729559748424], [1.77062742E12, 0.16299559471365638], [1.7706273E12, 1.1240875912408759]], "isOverall": false, "label": "Login Request", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77062742E12, "title": "Connect Time Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getConnectTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average Connect Time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendConnectTimeOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average connect time was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesConnectTimeOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotConnectTimeOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewConnectTimeOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Connect Time Over Time
function refreshConnectTimeOverTime(fixTimestamps) {
    var infos = connectTimeOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyConnectTimeOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 19800000);
    }
    if(isGraph($("#flotConnectTimeOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesConnectTimeOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotConnectTimeOverTime", "#overviewConnectTimeOverTime");
        $('#footerConnectTimeOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var responseTimePercentilesOverTimeInfos = {
        data: {"result": {"minY": 2541.0, "minX": 1.7706273E12, "maxY": 10447.0, "series": [{"data": [[1.77062736E12, 10447.0], [1.77062742E12, 10410.0], [1.7706273E12, 9519.0]], "isOverall": false, "label": "Max", "isController": false}, {"data": [[1.77062736E12, 8532.0], [1.77062742E12, 3014.0], [1.7706273E12, 2541.0]], "isOverall": false, "label": "Min", "isController": false}, {"data": [[1.77062736E12, 10028.6], [1.77062742E12, 10108.2], [1.7706273E12, 9174.4]], "isOverall": false, "label": "90th percentile", "isController": false}, {"data": [[1.77062736E12, 10408.26], [1.77062742E12, 10362.64], [1.7706273E12, 9508.74]], "isOverall": false, "label": "99th percentile", "isController": false}, {"data": [[1.77062736E12, 9225.0], [1.77062742E12, 8370.0], [1.7706273E12, 7878.0]], "isOverall": false, "label": "Median", "isController": false}, {"data": [[1.77062736E12, 10154.15], [1.77062742E12, 10203.2], [1.7706273E12, 9389.0]], "isOverall": false, "label": "95th percentile", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77062742E12, "title": "Response Time Percentiles Over Time (successful requests only)"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true,
                        fill: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Response Time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimePercentilesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Response time was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimePercentilesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimePercentilesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimePercentilesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Response Time Percentiles Over Time
function refreshResponseTimePercentilesOverTime(fixTimestamps) {
    var infos = responseTimePercentilesOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 19800000);
    }
    if(isGraph($("#flotResponseTimePercentilesOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesResponseTimePercentilesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimePercentilesOverTime", "#overviewResponseTimePercentilesOverTime");
        $('#footerResponseTimePercentilesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var responseTimeVsRequestInfos = {
    data: {"result": {"minY": 3094.5, "minX": 2.0, "maxY": 9681.5, "series": [{"data": [[2.0, 3094.5], [8.0, 9203.5], [33.0, 4723.0], [9.0, 9068.0], [10.0, 9141.5], [11.0, 9681.5], [3.0, 4773.0], [12.0, 9048.5], [13.0, 9201.0], [14.0, 9071.5], [15.0, 9531.5], [4.0, 5935.5], [16.0, 9106.0], [17.0, 8809.0], [18.0, 9199.5], [5.0, 7632.5], [21.0, 7417.0], [6.0, 8443.0], [7.0, 9186.5]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 1000, "maxX": 33.0, "title": "Response Time Vs Request"}},
    getOptions: function() {
        return {
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Response Time in ms",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: {
                noColumns: 2,
                show: true,
                container: '#legendResponseTimeVsRequest'
            },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median response time at %x req/s was %y ms"
            },
            colors: ["#9ACD32", "#FF6347"]
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesResponseTimeVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotResponseTimeVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewResponseTimeVsRequest"), dataset, prepareOverviewOptions(options));

    }
};

// Response Time vs Request
function refreshResponseTimeVsRequest() {
    var infos = responseTimeVsRequestInfos;
    prepareSeries(infos.data);
    if (isGraph($("#flotResponseTimeVsRequest"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimeVsRequest");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimeVsRequest", "#overviewResponseTimeVsRequest");
        $('#footerResponseRimeVsRequest .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var latenciesVsRequestInfos = {
    data: {"result": {"minY": 3094.0, "minX": 2.0, "maxY": 9681.0, "series": [{"data": [[2.0, 3094.0], [8.0, 9203.0], [33.0, 4723.0], [9.0, 9065.0], [10.0, 9141.5], [11.0, 9681.0], [3.0, 4750.0], [12.0, 9048.5], [13.0, 9201.0], [14.0, 9071.5], [15.0, 9531.5], [4.0, 5935.5], [16.0, 9106.0], [17.0, 8807.5], [18.0, 9199.5], [5.0, 7614.0], [21.0, 7401.0], [6.0, 8441.5], [7.0, 9186.0]], "isOverall": false, "label": "Successes", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 1000, "maxX": 33.0, "title": "Latencies Vs Request"}},
    getOptions: function() {
        return{
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Latency in ms",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: { noColumns: 2,show: true, container: '#legendLatencyVsRequest' },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median Latency time at %x req/s was %y ms"
            },
            colors: ["#9ACD32", "#FF6347"]
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesLatencyVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotLatenciesVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewLatenciesVsRequest"), dataset, prepareOverviewOptions(options));
    }
};

// Latencies vs Request
function refreshLatenciesVsRequest() {
        var infos = latenciesVsRequestInfos;
        prepareSeries(infos.data);
        if(isGraph($("#flotLatenciesVsRequest"))){
            infos.createGraph();
        }else{
            var choiceContainer = $("#choicesLatencyVsRequest");
            createLegend(choiceContainer, infos);
            infos.createGraph();
            setGraphZoomable("#flotLatenciesVsRequest", "#overviewLatenciesVsRequest");
            $('#footerLatenciesVsRequest .legendColorBox > div').each(function(i){
                $(this).clone().prependTo(choiceContainer.find("li").eq(i));
            });
        }
};

var hitsPerSecondInfos = {
        data: {"result": {"minY": 2.1166666666666667, "minX": 1.7706273E12, "maxY": 10.6, "series": [{"data": [[1.77062736E12, 10.6], [1.77062742E12, 2.1166666666666667], [1.7706273E12, 3.95]], "isOverall": false, "label": "hitsPerSecond", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77062742E12, "title": "Hits Per Second"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of hits / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendHitsPerSecond"
                },
                selection: {
                    mode : 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y.2 hits/sec"
                }
            };
        },
        createGraph: function createGraph() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesHitsPerSecond"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotHitsPerSecond"), dataset, options);
            // setup overview
            $.plot($("#overviewHitsPerSecond"), dataset, prepareOverviewOptions(options));
        }
};

// Hits per second
function refreshHitsPerSecond(fixTimestamps) {
    var infos = hitsPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 19800000);
    }
    if (isGraph($("#flotHitsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesHitsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotHitsPerSecond", "#overviewHitsPerSecond");
        $('#footerHitsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var codesPerSecondInfos = {
        data: {"result": {"minY": 2.283333333333333, "minX": 1.7706273E12, "maxY": 10.6, "series": [{"data": [[1.77062736E12, 10.6], [1.77062742E12, 3.783333333333333], [1.7706273E12, 2.283333333333333]], "isOverall": false, "label": "200", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77062742E12, "title": "Codes Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendCodesPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "Number of Response Codes %s at %x was %y.2 responses / sec"
                }
            };
        },
    createGraph: function() {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesCodesPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotCodesPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewCodesPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Codes per second
function refreshCodesPerSecond(fixTimestamps) {
    var infos = codesPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 19800000);
    }
    if(isGraph($("#flotCodesPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesCodesPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotCodesPerSecond", "#overviewCodesPerSecond");
        $('#footerCodesPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var transactionsPerSecondInfos = {
        data: {"result": {"minY": 2.283333333333333, "minX": 1.7706273E12, "maxY": 10.6, "series": [{"data": [[1.77062736E12, 10.6], [1.77062742E12, 3.783333333333333], [1.7706273E12, 2.283333333333333]], "isOverall": false, "label": "Login Request-success", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77062742E12, "title": "Transactions Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of transactions / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendTransactionsPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y transactions / sec"
                }
            };
        },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesTransactionsPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotTransactionsPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewTransactionsPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Transactions per second
function refreshTransactionsPerSecond(fixTimestamps) {
    var infos = transactionsPerSecondInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyTransactionsPerSecond");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 19800000);
    }
    if(isGraph($("#flotTransactionsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTransactionsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTransactionsPerSecond", "#overviewTransactionsPerSecond");
        $('#footerTransactionsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var totalTPSInfos = {
        data: {"result": {"minY": 2.283333333333333, "minX": 1.7706273E12, "maxY": 10.6, "series": [{"data": [[1.77062736E12, 10.6], [1.77062742E12, 3.783333333333333], [1.7706273E12, 2.283333333333333]], "isOverall": false, "label": "Transaction-success", "isController": false}, {"data": [], "isOverall": false, "label": "Transaction-failure", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77062742E12, "title": "Total Transactions Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of transactions / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendTotalTPS"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y transactions / sec"
                },
                colors: ["#9ACD32", "#FF6347"]
            };
        },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesTotalTPS"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotTotalTPS"), dataset, options);
        // setup overview
        $.plot($("#overviewTotalTPS"), dataset, prepareOverviewOptions(options));
    }
};

// Total Transactions per second
function refreshTotalTPS(fixTimestamps) {
    var infos = totalTPSInfos;
    // We want to ignore seriesFilter
    prepareSeries(infos.data, false, true);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 19800000);
    }
    if(isGraph($("#flotTotalTPS"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTotalTPS");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTotalTPS", "#overviewTotalTPS");
        $('#footerTotalTPS .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

// Collapse the graph matching the specified DOM element depending the collapsed
// status
function collapse(elem, collapsed){
    if(collapsed){
        $(elem).parent().find(".fa-chevron-up").removeClass("fa-chevron-up").addClass("fa-chevron-down");
    } else {
        $(elem).parent().find(".fa-chevron-down").removeClass("fa-chevron-down").addClass("fa-chevron-up");
        if (elem.id == "bodyBytesThroughputOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshBytesThroughputOverTime(true);
            }
            document.location.href="#bytesThroughputOverTime";
        } else if (elem.id == "bodyLatenciesOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesOverTime(true);
            }
            document.location.href="#latenciesOverTime";
        } else if (elem.id == "bodyCustomGraph") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshCustomGraph(true);
            }
            document.location.href="#responseCustomGraph";
        } else if (elem.id == "bodyConnectTimeOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshConnectTimeOverTime(true);
            }
            document.location.href="#connectTimeOverTime";
        } else if (elem.id == "bodyResponseTimePercentilesOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimePercentilesOverTime(true);
            }
            document.location.href="#responseTimePercentilesOverTime";
        } else if (elem.id == "bodyResponseTimeDistribution") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeDistribution();
            }
            document.location.href="#responseTimeDistribution" ;
        } else if (elem.id == "bodySyntheticResponseTimeDistribution") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshSyntheticResponseTimeDistribution();
            }
            document.location.href="#syntheticResponseTimeDistribution" ;
        } else if (elem.id == "bodyActiveThreadsOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshActiveThreadsOverTime(true);
            }
            document.location.href="#activeThreadsOverTime";
        } else if (elem.id == "bodyTimeVsThreads") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTimeVsThreads();
            }
            document.location.href="#timeVsThreads" ;
        } else if (elem.id == "bodyCodesPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshCodesPerSecond(true);
            }
            document.location.href="#codesPerSecond";
        } else if (elem.id == "bodyTransactionsPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTransactionsPerSecond(true);
            }
            document.location.href="#transactionsPerSecond";
        } else if (elem.id == "bodyTotalTPS") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTotalTPS(true);
            }
            document.location.href="#totalTPS";
        } else if (elem.id == "bodyResponseTimeVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeVsRequest();
            }
            document.location.href="#responseTimeVsRequest";
        } else if (elem.id == "bodyLatenciesVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesVsRequest();
            }
            document.location.href="#latencyVsRequest";
        }
    }
}

/*
 * Activates or deactivates all series of the specified graph (represented by id parameter)
 * depending on checked argument.
 */
function toggleAll(id, checked){
    var placeholder = document.getElementById(id);

    var cases = $(placeholder).find(':checkbox');
    cases.prop('checked', checked);
    $(cases).parent().children().children().toggleClass("legend-disabled", !checked);

    var choiceContainer;
    if ( id == "choicesBytesThroughputOverTime"){
        choiceContainer = $("#choicesBytesThroughputOverTime");
        refreshBytesThroughputOverTime(false);
    } else if(id == "choicesResponseTimesOverTime"){
        choiceContainer = $("#choicesResponseTimesOverTime");
        refreshResponseTimeOverTime(false);
    }else if(id == "choicesResponseCustomGraph"){
        choiceContainer = $("#choicesResponseCustomGraph");
        refreshCustomGraph(false);
    } else if ( id == "choicesLatenciesOverTime"){
        choiceContainer = $("#choicesLatenciesOverTime");
        refreshLatenciesOverTime(false);
    } else if ( id == "choicesConnectTimeOverTime"){
        choiceContainer = $("#choicesConnectTimeOverTime");
        refreshConnectTimeOverTime(false);
    } else if ( id == "choicesResponseTimePercentilesOverTime"){
        choiceContainer = $("#choicesResponseTimePercentilesOverTime");
        refreshResponseTimePercentilesOverTime(false);
    } else if ( id == "choicesResponseTimePercentiles"){
        choiceContainer = $("#choicesResponseTimePercentiles");
        refreshResponseTimePercentiles();
    } else if(id == "choicesActiveThreadsOverTime"){
        choiceContainer = $("#choicesActiveThreadsOverTime");
        refreshActiveThreadsOverTime(false);
    } else if ( id == "choicesTimeVsThreads"){
        choiceContainer = $("#choicesTimeVsThreads");
        refreshTimeVsThreads();
    } else if ( id == "choicesSyntheticResponseTimeDistribution"){
        choiceContainer = $("#choicesSyntheticResponseTimeDistribution");
        refreshSyntheticResponseTimeDistribution();
    } else if ( id == "choicesResponseTimeDistribution"){
        choiceContainer = $("#choicesResponseTimeDistribution");
        refreshResponseTimeDistribution();
    } else if ( id == "choicesHitsPerSecond"){
        choiceContainer = $("#choicesHitsPerSecond");
        refreshHitsPerSecond(false);
    } else if(id == "choicesCodesPerSecond"){
        choiceContainer = $("#choicesCodesPerSecond");
        refreshCodesPerSecond(false);
    } else if ( id == "choicesTransactionsPerSecond"){
        choiceContainer = $("#choicesTransactionsPerSecond");
        refreshTransactionsPerSecond(false);
    } else if ( id == "choicesTotalTPS"){
        choiceContainer = $("#choicesTotalTPS");
        refreshTotalTPS(false);
    } else if ( id == "choicesResponseTimeVsRequest"){
        choiceContainer = $("#choicesResponseTimeVsRequest");
        refreshResponseTimeVsRequest();
    } else if ( id == "choicesLatencyVsRequest"){
        choiceContainer = $("#choicesLatencyVsRequest");
        refreshLatenciesVsRequest();
    }
    var color = checked ? "black" : "#818181";
    if(choiceContainer != null) {
        choiceContainer.find("label").each(function(){
            this.style.color = color;
        });
    }
}

