/*
 * Copyright ixias.net All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package ixias.play.api.mvc

/**
 * Custom binders for Path, and Query String
 *
 * If you provide your own PathBindable or QueryStringBindable,
 * make sure PlayFramework knows to import them
 * in your routes file by using the routesImport SBT settings key.
 *
 * [ Hint ]
 *   RoutesKeys.routesImport := Seq("CustomBinder._")
 */
trait Binders extends binder.Box
    with binder.IdBindable
    with binder.CursorBindable
    with binder.JavaTimeBindable

