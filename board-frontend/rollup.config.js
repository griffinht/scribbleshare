import {nodeResolve} from '@rollup/plugin-node-resolve';
import {terser} from "rollup-plugin-terser";

export default {
  input: 'src/scripts/main.js',
  output: {
    file: 'dist/src/scripts/main.js',
    format: 'iife',
  },
  plugins: [
    nodeResolve(),
    terser(),
  ]
};
