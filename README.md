# Collision Check

*Collision Check* is a calculator for estimating the probability of collision between two conjuncting satellites, using information found in a standard [Conjunction Summary Message (CSM)](https://www.space-track.org/documents/CSM_Guide.pdf). This is accomplished via [Monte Carlo simulation](http://en.wikipedia.org/wiki/Monte_Carlo_method), using the [Cholesky decomposition](http://en.wikipedia.org/wiki/Cholesky_decomposition) of the conjuncting satellites' [dispersion adjusted](http://en.wikipedia.org/wiki/Standard_deviation) covariance matrices sampled over a random [Gaussian distribution](http://en.wikipedia.org/wiki/Normal_distribution).

The program can be downloaded in the [releases](https://github.com/david-rc-dayton/collision-check/releases) tab.

# Screenshots

![calculator](https://raw.githubusercontent.com/david-rc-dayton/collision-check/master/screenshots/calculator_screenshot.png)
![cdf](https://raw.githubusercontent.com/david-rc-dayton/collision-check/master/screenshots/cdf_screenshot.png)
![scatter](https://raw.githubusercontent.com/david-rc-dayton/collision-check/master/screenshots/scatter_screenshot.png)


## Building

To download the required dependencies and build the *Collision Check* program using [Leiningen](http://leiningen.org/), run the following command while in the project root directory:

`lein uberjar`

The program will packaged as a standalone JAR file in the `./target` directory.

## Usage

*This program requires an installed [Java Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/index.html), version 7 or higher.*

To run the program, double click the `collision-check-XXX-standalone.jar` (if your operating system's file manager is configured to launch Java programs), or by entering the following in the command line (replacing `XXX` with the version number):

`java -jar collision-check-XXX-standalone.jar`

Once the application has started; 
- Enter the UVW coordinates from the CSM in the `Asset Position` and `Satellite Postion` fields.
- Enter the 3x3 UVW covariance matrices from the CSM in the `Asset Covariance` and `Satellite Covariance` fields. 
- Enter the combined radii (in meters) of the satelites in the `Combined Radii` field, to define the collision space. **The radar cross-section from the CSM is not a reliable measure of the actual satellite cross section. It is safer to over-estimate, than under-estimate.**
- Enter a [standard deviation](http://en.wikipedia.org/wiki/Standard_deviation) in the `Sigma` field, to define the conjunction space. A low sigma (e.g. 1) will result in a small search space, with high fidelity in a close approach. A high sigma (e.g. 3) will increase the search space, but will dilute the collision probability.
- Enter the number of samples to take in the conjunction space. This number should be fairly large (many tens of thousands).
- Click the `Run` button.

Once the `Run` button is pressed, the program will check to ensure that the entered position and covariance appear valid. If values seem invalid, an alert will appear indicating the incorrect field(s). If any values cannot be parsed, relatively sane defaults will be used in the calculation; an alert will appear indicating the default value.

If all values are determined to be valid, the progress bar above the `Run` button will begin to increment as the conjunction space is sampled. Upon sampling completion, several charts will be created:
- `Miss-Distance Cumulative Distribution`: Shows the [cumulative distribtion](http://en.wikipedia.org/wiki/Cumulative_distribution_function) of the miss distances calculated during the simulation. A vertical bar is also included to indicate the collision space boundry. Also contained in the chart are the `CSM Miss Distance`, to compare with the miss distance found in the CSM, and the `Collision Probability`.
- `UVW-Axis Conjunction Space`: Several charts showing a subset of the points sampled in the conjunction space. This provides a visual representation of the position and covariance in the Radial, In-Track, and Cross-Track planes. The asset's sampled points will appear red, and the satellite's sampled points will appear blue.

The charts can be saved or manipulated by right-clicking inside the chart area and selecting from the available options. Zooming in is accomplished by higlighting a area on the chart with the mouse cursor; clicking and dragging up will reset the chart's zoom.

A Collision Avoidance (ColA) Maneuver should probably be considered when the `Collision Probability` is calculated to be above some threshold. An industry standard threshold is **1e-04**, a 1 in 10,000 chance of colliding.

## Credits
- *Collision Check* program icon obtained from [IconArchive](http://www.iconarchive.com/show/space-icons-by-aha-soft/supernova-icon.html).

## License

The MIT License (MIT)

Copyright (c) 2014 David RC Dayton

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
