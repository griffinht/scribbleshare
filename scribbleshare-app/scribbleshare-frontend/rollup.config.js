import {nodeResolve} from '@rollup/plugin-node-resolve';
import typescript from '@rollup/plugin-typescript';
import {terser} from "rollup-plugin-terser";

export default {
  input: 'src/scripts/main.ts',
  output: {
    file: 'build/scripts/main.js',
    format: 'iife',
  },
  plugins: [
    nodeResolve(),
    terser({
      mangle: {
        properties:true,
      },
    }),
    typescript(),
  ]
};
