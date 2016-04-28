A lib to animate a path
===


![image](http://yourbay.me/media/imgs/vivid-path-vivid-view.gif)

`app` is a demo.

`lib` is the core code.

### Usage

- VividDrawable

```Java
VividDrawable vividDrawable1 = new VividDrawable(path);
((ImageView) findViewById(R.id.iv_vividPath)).setImageDrawable(vividDrawable1);
```

- VividView

```Java
((VividView) findViewById(R.id.iv_vividView)).setPath(path);
```


## License

    Copyright 2016 vividPath

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.